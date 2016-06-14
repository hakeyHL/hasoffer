package hasoffer.core.push;

import hasoffer.core.bo.push.AppPushBo;

/**
 * Date : 2016/4/27
 * Function :
 */
public interface IPushService {

    void push(String to, AppPushBo pushBo);

}
