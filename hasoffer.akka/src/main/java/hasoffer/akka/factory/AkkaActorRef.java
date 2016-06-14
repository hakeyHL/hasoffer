package hasoffer.akka.factory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import hasoffer.akka.actor.MasterActor;

/*
 * 文件名： ActorFactory.java
 * 
 * 工程名称: spring-akka
 *
 * 创建消息的入口。
 *
 * 创建日期： 2016年04月21日
 *
 * Copyright(C) 2015, by hasoffer
 *
 * 原始作者: zhouwendong
 *
 */
public class AkkaActorRef {

    private ActorRef akkaActorRef;
    private ActorSystem actorSystem;

    public static ActorRef getDefaultActorRef() {
        return SingletonHolder.INSTANCE.getAkkaActorRef();
    }

    public static  ActorSystem getDefaultActorSystem(){
        return SingletonHolder.INSTANCE.getActorSystem();
    }

    private AkkaActorRef() {
//        ActorSystem system = ActorSystem.create("ServerApp", ConfigFactory.load("akka.conf/server").getConfig("ServerSocketApp"));
        actorSystem = ActorSystem.create("ServerApp");
        akkaActorRef = actorSystem.actorOf(Props.create(MasterActor.class), "masterActor");
    }

    private ActorRef getAkkaActorRef() {
        return akkaActorRef;
    }


    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    private static class SingletonHolder {
        private static final AkkaActorRef INSTANCE = new AkkaActorRef();
    }

}
