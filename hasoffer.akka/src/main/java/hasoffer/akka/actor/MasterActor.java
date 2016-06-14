package hasoffer.akka.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import hasoffer.akka.pojo.AkkaJobConfigMessage;
import hasoffer.akka.pojo.AkkaJobMessage;

import java.util.HashMap;
import java.util.Map;

public class MasterActor extends UntypedActor {

    private Map<String, ActorRef> actorRefMap = new HashMap<String, ActorRef>();

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof AkkaJobConfigMessage){
            final AkkaJobConfigMessage beanObj = (AkkaJobConfigMessage) message;

            String actorName = beanObj.getClazz().getName();
            ActorRef actorRef = actorRefMap.get(actorName);
            if (actorRef == null) {
                Props props = Props.create(WorkerRouterActor.class, new Creator<WorkerRouterActor>() {
                    @Override
                    public WorkerRouterActor create() throws Exception {
                        return new WorkerRouterActor(beanObj.getThreadCount(),beanObj.getClazz());
                    }
                });
                actorRef = getContext().actorOf(props, actorName);
                actorRefMap.put(actorName, actorRef);
            }
        }else if (message instanceof AkkaJobMessage){
            final AkkaJobMessage beanObj = (AkkaJobMessage) message;
            String actorName = beanObj.getClazz().getName();
            ActorRef actorRef = actorRefMap.get(actorName);
            if (actorRef != null) {
                actorRef.tell(beanObj, ActorRef.noSender());
            }
        }else{
            unhandled(message);
        }
    }

}
