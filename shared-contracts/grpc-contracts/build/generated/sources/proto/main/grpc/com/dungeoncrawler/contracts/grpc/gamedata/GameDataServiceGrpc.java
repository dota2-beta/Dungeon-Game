package com.dungeoncrawler.contracts.grpc.gamedata;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class GameDataServiceGrpc {

  private GameDataServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gameDataService.GameDataService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest,
      com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> getGetEntityTemplateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEntityTemplate",
      requestType = com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest,
      com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> getGetEntityTemplateMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest, com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> getGetEntityTemplateMethod;
    if ((getGetEntityTemplateMethod = GameDataServiceGrpc.getGetEntityTemplateMethod) == null) {
      synchronized (GameDataServiceGrpc.class) {
        if ((getGetEntityTemplateMethod = GameDataServiceGrpc.getGetEntityTemplateMethod) == null) {
          GameDataServiceGrpc.getGetEntityTemplateMethod = getGetEntityTemplateMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest, com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEntityTemplate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameDataServiceMethodDescriptorSupplier("GetEntityTemplate"))
              .build();
        }
      }
    }
    return getGetEntityTemplateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest,
      com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> getGetGameMapMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGameMap",
      requestType = com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest,
      com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> getGetGameMapMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest, com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> getGetGameMapMethod;
    if ((getGetGameMapMethod = GameDataServiceGrpc.getGetGameMapMethod) == null) {
      synchronized (GameDataServiceGrpc.class) {
        if ((getGetGameMapMethod = GameDataServiceGrpc.getGetGameMapMethod) == null) {
          GameDataServiceGrpc.getGetGameMapMethod = getGetGameMapMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest, com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetGameMap"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameDataServiceMethodDescriptorSupplier("GetGameMap"))
              .build();
        }
      }
    }
    return getGetGameMapMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> getGetPlayerClassesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPlayerClasses",
      requestType = com.google.protobuf.Empty.class,
      responseType = com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> getGetPlayerClassesMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> getGetPlayerClassesMethod;
    if ((getGetPlayerClassesMethod = GameDataServiceGrpc.getGetPlayerClassesMethod) == null) {
      synchronized (GameDataServiceGrpc.class) {
        if ((getGetPlayerClassesMethod = GameDataServiceGrpc.getGetPlayerClassesMethod) == null) {
          GameDataServiceGrpc.getGetPlayerClassesMethod = getGetPlayerClassesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPlayerClasses"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameDataServiceMethodDescriptorSupplier("GetPlayerClasses"))
              .build();
        }
      }
    }
    return getGetPlayerClassesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GameDataServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameDataServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameDataServiceStub>() {
        @java.lang.Override
        public GameDataServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameDataServiceStub(channel, callOptions);
        }
      };
    return GameDataServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static GameDataServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameDataServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameDataServiceBlockingV2Stub>() {
        @java.lang.Override
        public GameDataServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameDataServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return GameDataServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GameDataServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameDataServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameDataServiceBlockingStub>() {
        @java.lang.Override
        public GameDataServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameDataServiceBlockingStub(channel, callOptions);
        }
      };
    return GameDataServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GameDataServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameDataServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameDataServiceFutureStub>() {
        @java.lang.Override
        public GameDataServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameDataServiceFutureStub(channel, callOptions);
        }
      };
    return GameDataServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getEntityTemplate(com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetEntityTemplateMethod(), responseObserver);
    }

    /**
     */
    default void getGameMap(com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetGameMapMethod(), responseObserver);
    }

    /**
     */
    default void getPlayerClasses(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPlayerClassesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service GameDataService.
   */
  public static abstract class GameDataServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return GameDataServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service GameDataService.
   */
  public static final class GameDataServiceStub
      extends io.grpc.stub.AbstractAsyncStub<GameDataServiceStub> {
    private GameDataServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameDataServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameDataServiceStub(channel, callOptions);
    }

    /**
     */
    public void getEntityTemplate(com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetEntityTemplateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getGameMap(com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetGameMapMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPlayerClasses(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPlayerClassesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service GameDataService.
   */
  public static final class GameDataServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<GameDataServiceBlockingV2Stub> {
    private GameDataServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameDataServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameDataServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse getEntityTemplate(com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetEntityTemplateMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse getGameMap(com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetGameMapMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse getPlayerClasses(com.google.protobuf.Empty request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetPlayerClassesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service GameDataService.
   */
  public static final class GameDataServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<GameDataServiceBlockingStub> {
    private GameDataServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameDataServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameDataServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse getEntityTemplate(com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetEntityTemplateMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse getGameMap(com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetGameMapMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse getPlayerClasses(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPlayerClassesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service GameDataService.
   */
  public static final class GameDataServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<GameDataServiceFutureStub> {
    private GameDataServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameDataServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameDataServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse> getEntityTemplate(
        com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetEntityTemplateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse> getGameMap(
        com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetGameMapMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> getPlayerClasses(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPlayerClassesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_ENTITY_TEMPLATE = 0;
  private static final int METHODID_GET_GAME_MAP = 1;
  private static final int METHODID_GET_PLAYER_CLASSES = 2;

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
        case METHODID_GET_ENTITY_TEMPLATE:
          serviceImpl.getEntityTemplate((com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse>) responseObserver);
          break;
        case METHODID_GET_GAME_MAP:
          serviceImpl.getGameMap((com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse>) responseObserver);
          break;
        case METHODID_GET_PLAYER_CLASSES:
          serviceImpl.getPlayerClasses((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>) responseObserver);
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
          getGetEntityTemplateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcRequest,
              com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse>(
                service, METHODID_GET_ENTITY_TEMPLATE)))
        .addMethod(
          getGetGameMapMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamedata.GameMapRequest,
              com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse>(
                service, METHODID_GET_GAME_MAP)))
        .addMethod(
          getGetPlayerClassesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>(
                service, METHODID_GET_PLAYER_CLASSES)))
        .build();
  }

  private static abstract class GameDataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GameDataServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.dungeoncrawler.contracts.grpc.gamedata.Gamedata.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GameDataService");
    }
  }

  private static final class GameDataServiceFileDescriptorSupplier
      extends GameDataServiceBaseDescriptorSupplier {
    GameDataServiceFileDescriptorSupplier() {}
  }

  private static final class GameDataServiceMethodDescriptorSupplier
      extends GameDataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    GameDataServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (GameDataServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GameDataServiceFileDescriptorSupplier())
              .addMethod(getGetEntityTemplateMethod())
              .addMethod(getGetGameMapMethod())
              .addMethod(getGetPlayerClassesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
