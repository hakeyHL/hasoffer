package hasoffer.affiliate.test;

import hasoffer.affiliate.affs.snapdeal.SnapdealProductProcessor;
import hasoffer.affiliate.affs.snapdeal.model.SnapDealAffiliateOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnapdealTest {
    public static void main(String[] args) {
        SnapdealProductProcessor productProcessor = new SnapdealProductProcessor();
        Map<String,String> paraMap=new HashMap<String, String>();
        paraMap.put("startDate", "2016-05-29");
        paraMap.put("endDate", "2016-05-29");
        paraMap.put("status", "approved");
//        status=approved

        List<SnapDealAffiliateOrder> affiliateOrderList = productProcessor.getAffiliateOrderList(paraMap);
        System.out.println(affiliateOrderList.size());
    }

}
