package hasoffer.akka.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.DefaultResizer;
import akka.routing.RoundRobinPool;
import hasoffer.akka.pojo.AkkaJobMessage;

public class WorkerRouterActor extends UntypedActor {

    private final ActorRef workerRouter;

    public WorkerRouterActor(final int nrOfWorkers, Class<?> clazz) {
        int lowerBound = 1;
        int upperBound = 10;
        DefaultResizer resizer = new DefaultResizer(lowerBound, upperBound);
        workerRouter = this.getContext().actorOf(Props.create(clazz).withRouter(new RoundRobinPool(nrOfWorkers).withResizer(resizer)), "workerRouter");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AkkaJobMessage) {
            workerRouter.tell(message, ActorRef.noSender());
        } else {
            unhandled(message);
        }
    }

}
