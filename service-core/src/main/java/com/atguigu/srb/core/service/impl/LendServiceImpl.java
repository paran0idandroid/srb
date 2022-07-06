package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exception.BusinessException;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.*;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.record.DVALRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Jon
 * @since 2021-04-20
 */
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);
        lend.setPublishDate(LocalDateTime.now());
        //起息日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dateTimeFormatter);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());

        //平台预期收益 = 标的金额 * (年化 / 12 * 期数)
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate.multiply(new BigDecimal(lend.getPeriod())));
        lend.setExpectAmount(expectAmount);

        //实际收益
        lend.setRealAmount(new BigDecimal(0));
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        lend.setCheckTime(LocalDateTime.now()); //审核时间
        lend.setCheckAdminId(1L);

        baseMapper.insert(lend);
    }

    @Override
    public List<Lend> selectList() {

        List<Lend> lendList = baseMapper.selectList(null);
        lendList.forEach(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = dictService.getNameByParentDictCodeAndValue("status", lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        });
        return lendList;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {

        //查询标的对象
        Lend lend = baseMapper.selectById(id);
        //组装数据
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        //根据user_id获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        //组装借款人对象
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        //组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);


        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {

        BigDecimal interestCount;
        if(returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()){
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        }else if(returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()){
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        }else if(returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()){
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        }else{
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }

        return interestCount;
    }

    @Override
    public void makeLoan(Long id) {

        //获取标的信息
        Lend lend = baseMapper.selectById(id);
        Integer status = lend.getStatus();

        //调用汇付宝放款接口
        Map<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentProjectCode", lend.getLendNo());
        //流水号唯一
        map.put("agentBillNo", LendNoUtils.getLoanNo());

        //月年化
        BigDecimal monthRate = lend.getServiceRate()
                .divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台服务费：已投金额 * 月年化 * 投资时长
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        map.put("mchFee", realAmount);

        map.put("timestamp", RequestHelper.getTimestamp());
        map.put("sign", RequestHelper.getSign(map));

        //提交远程请求
        JSONObject result = RequestHelper.sendRequest(map, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + result.toJSONString());

        //放款失败抛出异常
        if(!"0000".equals(result.getString("resultCode"))){
            throw new BusinessException((String) result.get("resultMsg"));
        }

        //放款成功
        //（1）标的状态和标的平台收益：更新标的相关信息
        lend.setRealAmount(realAmount); //平台收益
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(lend);

        //（2）给借款账号转入金额
        //获取借款人bindCode
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();
        BigDecimal voteAmt = new BigDecimal(result.getString("voteAmt"));
        userAccountMapper.updateAccount(
                bindCode,
                voteAmt,
                new BigDecimal(0));

        //（3）增加借款交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                result.getString("agentBillNo"),
                bindCode,
                voteAmt,
                TransTypeEnum.BORROW_BACK,
                "项目放款，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
                );
        transFlowService.saveTransFlow(transFlowBO);

        //（4）解冻并扣除投资人资金
        //先获取投资人列表
        List<LendItem> lendItemList = lendItemService.selectByLendId(id, 1);
        lendItemList.stream().forEach(item -> {
            Long investUserId = item.getInvestUserId();
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserInfo.getBindCode();
            BigDecimal investAmount = item.getInvestAmount();
            userAccountMapper.updateAccount(
                    investBindCode,
                    new BigDecimal(0),
                    investAmount.negate());

            //（5）增加投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "项目放款，冻结资金转出，项目编号：" + lend.getLendNo()
                            + "，项目名称：" + lend.getTitle()
                    );
            transFlowService.saveTransFlow(investTransFlowBO);
        });


        //（6）生成借款人还款计划和出借人回款计划
        //放款成功生成借款人还款计划和投资人回款计划
        this.repaymentPlan(lend);

    }

    /**
     * 还款计划
     *
     * @param lend
     */
    private void repaymentPlan(Lend lend){

        //3期10个投资人
        //每一期的还款拆分成10份
        /**
         * 创建还款计划列表
         * 根据还款期限生成还款计划 for-loop
         * {
         *     创建还款计划对象
         *     设置基本属性
         *     判断是否为最后一个期
         *     设置还款状态
         *     将还款对象加入还款计划列表
         * }
         *
         * 批量保存
         *
         * 期数做键，还款记录的id做值，组装HashMap
         */
        //还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();
        for (int i = 1; i <= len; i++) {

            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());

            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);

            //判断是否是最后一期
            if(i == len){
                lendReturn.setLast(true);
            }else{
                lendReturn.setLast(false);
            }

            //设置还款状态
            lendReturn.setStatus(0);

            //将还款对象加入还款计划列表
            lendReturnList.add(lendReturn);
        }

        //批量保存还款计划
        lendReturnService.saveBatch(lendReturnList);

        //生成期数和还款记录id对应的键值对集合
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        //创建所有投资的全部回款记录
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        //获取当前标的下所有的已支付投资
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);


        for (LendItem lendItem : lendItemList) {
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        for (LendReturn lendReturn : lendReturnList) {

            BigDecimal sumPrincipal = lendItemReturnAllList
                    .stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList
                    .stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal total = lendItemReturnAllList
                    .stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //本金 利息 总额
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(total);
        }

        //批量更新
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划（针对某一笔投资的回款计划）
     *
     * @param lendItemId
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend){

        //获取当前投资记录信息 lendItem：一笔投资
        LendItem lendItem = lendItemService.getById(lendItemId);

        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {

            Integer currentPeriod = entry.getKey(); //当前期数
            Long lendReturnId = lendReturnMap.get(currentPeriod); //还款计划id

            //创建回款计划
            LendItemReturn lendItemReturn = new LendItemReturn();
            //将还款记录lendReturn 关联 回款记录lendItemReturn
            lendItemReturn.setLendReturnId(lendReturnId);
            //设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            //计算回款本金、利息和总额（注意最后一个月）
            if(currentPeriod.intValue() == lend.getPeriod().intValue()){
                //本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);

            }else{
                //本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                //利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            //总金额
            lendItemReturn
                    .setTotal(lendItemReturn.getPrincipal()
                            .add(lendItemReturn.getInterest()));
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            //是否逾期，默认未逾期
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);
            //将回款记录放入回款列表
            lendItemReturnList.add(lendItemReturn);
        }
        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }


    /**
     * 一个投资人的所有回款 lendItemReturnList
     * 所有投资人的所有回款 lendItemReturnAllList
     * 计算借款人的每一笔还款：所有投资人其中的一期回款
     * 还款计划 1 ： 回款计划 n
     *
     * 把所有投资者的所有回款记录取出lendItemReturnAllList
     * 遍历还款列表
     * 过滤：把当前这期还款对象对应的所有回款记录找出
     * 找数据：找出需要处理的属性
     * 做数据处理：将找出的数据相加
     */


}