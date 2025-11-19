package com.dungeoncrawler.contracts.grpc.gamesession;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class GameSessionServiceGrpc {

  private GameSessionServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gameSessionService.GameSessionService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> getJoinSessionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "JoinSession",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> getJoinSessionMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest, com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> getJoinSessionMethod;
    if ((getJoinSessionMethod = GameSessionServiceGrpc.getJoinSessionMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getJoinSessionMethod = GameSessionServiceGrpc.getJoinSessionMethod) == null) {
          GameSessionServiceGrpc.getJoinSessionMethod = getJoinSessionMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest, com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "JoinSession"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("JoinSession"))
              .build();
        }
      }
    }
    return getJoinSessionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GameSessionServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceStub>() {
        @java.lang.Override
        public GameSessionServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameSessionServiceStub(channel, callOptions);
        }
      };
    return GameSessionServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static GameSessionServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceBlockingV2Stub>() {
        @java.lang.Override
        public GameSessionServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameSessionServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return GameSessionServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GameSessionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceBlockingStub>() {
        @java.lang.Override
        public GameSessionServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameSessionServiceBlockingStub(channel, callOptions);
        }
      };
    return GameSessionServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GameSessionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameSessionServiceFutureStub>() {
        @java.lang.Override
        public GameSessionServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameSessionServiceFutureStub(channel, callOptions);
        }
      };
    return GameSessionServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void joinSession(com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getJoinSessionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service GameSessionService.
   */
  public static abstract class GameSessionServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return GameSessionServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service GameSessionService.
   */
  public static final class GameSessionServiceStub
      extends io.grpc.stub.AbstractAsyncStub<GameSessionServiceStub> {
    private GameSessionServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameSessionServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameSessionServiceStub(channel, callOptions);
    }

    /**
     */
    public void joinSession(com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getJoinSessionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service GameSessionService.
   */
  public static final class GameSessionServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<GameSessionServiceBlockingV2Stub> {
    private GameSessionServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameSessionServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameSessionServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse joinSession(com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getJoinSessionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service GameSessionService.
   */
  public static final class GameSessionServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<GameSessionServiceBlockingStub> {
    private GameSessionServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameSessionServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameSessionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse joinSession(com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getJoinSessionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service GameSessionService.
   */
  public static final class GameSessionServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<GameSessionServiceFutureStub> {
    private GameSessionServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameSessionServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameSessionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse> joinSession(
        com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getJoinSessionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_JOIN_SESSION = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_JOIN_SESSION:
          serviceImpl.joinSession((com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getJoinSessionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse>(
                service, METHODID_JOIN_SESSION)))
        .build();
  }

  private static abstract class GameSessionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GameSessionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.dungeoncrawler.contracts.grpc.gamesession.Gamesession.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GameSessionService");
    }
  }

  private static final class GameSessionServiceFileDescriptorSupplier
      extends GameSessionServiceBaseDescriptorSupplier {
    GameSessionServiceFileDescriptorSupplier() {}
  }

  private static final class GameSessionServiceMethodDescriptorSupplier
      extends GameSessionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    GameSessionServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GameSessionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GameSessionServiceFileDescriptorSupplier())
              .addMethod(getJoinSessionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
