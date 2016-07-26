package hasoffer.core.app;

import hasoffer.core.persistence.po.admin.Advertisement;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hs on 2016年07月26日.
 * Time 13:04
 */
@Service
public interface AdvertiseService {

    public List<Advertisement> getAdByCategory();
}
