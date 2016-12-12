package hasoffer.core.app.impl;

import hasoffer.core.app.AppSearchService;
import hasoffer.core.app.vo.ProductListVo;
import hasoffer.core.bo.system.SearchCriteria;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hs on 2016年12月12日.
 * Time 14:48
 */
@Service
public class AppSearchServiceImpl implements AppSearchService {
    @Override
    public List<ProductListVo> filterByParams(SearchCriteria searchCriteria) {
        //1. brand
        //2. network
        //3. screenResolution
        //4. operatingSystem
        //5. expandableMemory
        //6. ram
        //7. batteryCapacity
        //8. internalMemory
        //9. primaryCamera
        //10.secondaryCamera
        //11.screenSize

        return null;
    }
}
