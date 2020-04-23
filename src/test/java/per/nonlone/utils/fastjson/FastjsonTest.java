package per.nonlone.utils.fastjson;

import org.junit.Test;

public class FastjsonTest {

    @Test
    public void parseObjectRecurrence(){

        String data = "{\n" +
                "    \"code\": \"200\",\n" +
                "    \"message\": \"success\",\n" +
                "    \"data\": \"{\\\"returnResult\\\":{\\\"DECISION_ID\\\":\\\"bAlxyRZw5oMaHYBfaHGpsH\\\",\\\"DECISION_RESPONSE\\\":{\\\"telInvestResult\\\":\\\"P\\\",\\\"maxCalCreditAmt\\\":700000.0,\\\"riskScoreResult\\\":\\\"P\\\",\\\"versionID\\\":\\\"V1.07_20181220\\\",\\\"morLoanPMT\\\":10000.0,\\\"creLoanPMT\\\":3000.0,\\\"ruleDecision\\\":[],\\\"telInvestFlag\\\":\\\"0\\\",\\\"xscoreWanted\\\":\\\"1\\\",\\\"paramMap\\\":{\\\"applyId\\\":\\\"FT133532775637581824\\\",\\\"engineNode\\\":\\\"FT010\\\",\\\"systemId\\\":\\\"FEITAI\\\"},\\\"morLoanType\\\":\\\"01\\\",\\\"productDetail\\\":[],\\\"scoreModelReturnInfo\\\":[],\\\"ficreLoan\\\":200000.0,\\\"productSelected\\\":[{\\\"loanterm\\\":0,\\\"fundProvider\\\":\\\"\\\",\\\"riskLevel\\\":\\\"SuperC\\\",\\\"partnerName\\\":\\\"\\\",\\\"creditEffectiveDays\\\":25.0,\\\"bkriskFlag\\\":\\\"1\\\",\\\"primaryProduct\\\":\\\"\\\",\\\"productLevel\\\":\\\"H\\\",\\\"guaranteeType\\\":\\\"\\\",\\\"dtiline\\\":200000.0,\\\"entryFlag\\\":\\\"1\\\",\\\"riskRateLevel\\\":\\\"P08\\\",\\\"calCreditAmt\\\":100000.0},{\\\"loanterm\\\":0,\\\"fundProvider\\\":\\\"\\\",\\\"riskLevel\\\":\\\"SuperC\\\",\\\"partnerName\\\":\\\"\\\",\\\"creditEffectiveDays\\\":25.0,\\\"bkriskFlag\\\":\\\"1\\\",\\\"primaryProduct\\\":\\\"\\\",\\\"productLevel\\\":\\\"H\\\",\\\"guaranteeType\\\":\\\"\\\",\\\"dtiline\\\":200000.0,\\\"entryFlag\\\":\\\"1\\\",\\\"riskRateLevel\\\":\\\"P08\\\",\\\"calCreditAmt\\\":100000.0}],\\\"creLoanBalance\\\":700000.0,\\\"investigateFlag\\\":\\\"0\\\",\\\"score03\\\":500.0,\\\"icdecision\\\":[{\\\"ruleCode\\\":\\\"DH025\\\",\\\"ruleAction\\\":\\\"检查申请人手机号\\\",\\\"ruleDecisionResult\\\":\\\"电核\\\",\\\"ruleSeverityLevel\\\":1},{\\\"ruleCode\\\":\\\"DH032\\\",\\\"ruleAction\\\":\\\"身份证45开头\\\",\\\"ruleDecisionResult\\\":\\\"电核\\\",\\\"ruleSeverityLevel\\\":1},{\\\"ruleCode\\\":\\\"DH997\\\",\\\"ruleDecisionResult\\\":\\\"免核\\\",\\\"ruleSeverityLevel\\\":0},{\\\"ruleCode\\\":\\\"DC018\\\",\\\"ruleAction\\\":\\\"核查申请人手机号并且核查申请人与联系人的关系\\\",\\\"ruleDecisionResult\\\":\\\"调查\\\",\\\"ruleSeverityLevel\\\":1},{\\\"ruleCode\\\":\\\"DH001\\\",\\\"ruleDecisionResult\\\":\\\"电核\\\",\\\"ruleSeverityLevel\\\":1}],\\\"score02\\\":500.0,\\\"carLoanPMT\\\":5000.0,\\\"score04\\\":500.0,\\\"score01\\\":500.0,\\\"finalResult\\\":\\\"P\\\"},\\\"RETMSG\\\":\\\"SUC000-处理成功\\\",\\\"RETCODE\\\":\\\"SUC000\\\"},\\\"fullName\\\":\\\"陈木花\\\",\\\"idNum\\\":\\\"440882199407093028\\\"}\",\n" +
                "    \"traceId\": \"pgiI3xakQcapdyvGD-0t0Q\",\n" +
                "    \"timestamp\": 1548072000990,\n" +
                "    \"version\": 4,\n" +
                "    \"success\": true\n" +
                "}";

        System.out.println(JSON.toJSONString(JSON.parseObjectRecurrence(data)));
    }


}