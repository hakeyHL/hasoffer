package hasoffer.api.helper;

import akka.actor.ActorRef;
import hasoffer.akka.factory.AkkaActorRef;
import hasoffer.akka.pojo.AkkaJobConfigMessage;
import hasoffer.akka.pojo.AkkaJobMessage;
import hasoffer.core.app.vo.SearchIO;
import hasoffer.core.bo.system.SearchLogBo;
import hasoffer.webcommon.akka.SearchLogActor;

/**
 * Date : 2016/5/27
 * Function :
 */
public class SearchHelper {

    public static void addToLog(SearchIO sio) {
        if (sio.getCliSite() != null) {
            SearchLogBo searchLogBo = null;
            try {
                searchLogBo = new SearchLogBo(sio.getCliSourceId(), sio.getCliQ(), sio.getCliQBrand(), sio.getCliSite().name(), sio.getHsCateId(), sio.getHsProId(), sio.getHsSkuId(), sio.getCliPrice());
            } catch (Exception e) {
                System.out.println("new SearchLogBo exception , cmp/cmpsku");
                return;
            }
            ActorRef defaultActorRef = AkkaActorRef.getDefaultActorRef();
            AkkaJobConfigMessage message = new AkkaJobConfigMessage(SearchLogActor.class, 1);
            defaultActorRef.tell(message, ActorRef.noSender());
            AkkaJobMessage akkaJobMessage = new AkkaJobMessage(SearchLogActor.class, searchLogBo);
            defaultActorRef.tell(akkaJobMessage, ActorRef.noSender());
        }
    }

}
