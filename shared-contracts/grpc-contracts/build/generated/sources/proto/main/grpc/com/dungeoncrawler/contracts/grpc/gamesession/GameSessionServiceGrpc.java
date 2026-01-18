package com.dungeoncrawler.contracts.grpc.gamesession;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class GameSessionServiceGrpc {

  private GameSessionServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gameSessionService.GameSessionService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> getJoinSessionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "JoinSession",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> getJoinSessionMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> getJoinSessionMethod;
    if ((getJoinSessionMethod = GameSessionServiceGrpc.getJoinSessionMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getJoinSessionMethod = GameSessionServiceGrpc.getJoinSessionMethod) == null) {
          GameSessionServiceGrpc.getJoinSessionMethod = getJoinSessionMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "JoinSession"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("JoinSession"))
              .build();
        }
      }
    }
    return getJoinSessionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> getCreateSessionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateSession",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> getCreateSessionMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> getCreateSessionMethod;
    if ((getCreateSessionMethod = GameSessionServiceGrpc.getCreateSessionMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getCreateSessionMethod = GameSessionServiceGrpc.getCreateSessionMethod) == null) {
          GameSessionServiceGrpc.getCreateSessionMethod = getCreateSessionMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateSession"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("CreateSession"))
              .build();
        }
      }
    }
    return getCreateSessionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getMoveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Move",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getMoveMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getMoveMethod;
    if ((getMoveMethod = GameSessionServiceGrpc.getMoveMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getMoveMethod = GameSessionServiceGrpc.getMoveMethod) == null) {
          GameSessionServiceGrpc.getMoveMethod = getMoveMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Move"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("Move"))
              .build();
        }
      }
    }
    return getMoveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getAttackMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Attack",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getAttackMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getAttackMethod;
    if ((getAttackMethod = GameSessionServiceGrpc.getAttackMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getAttackMethod = GameSessionServiceGrpc.getAttackMethod) == null) {
          GameSessionServiceGrpc.getAttackMethod = getAttackMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Attack"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("Attack"))
              .build();
        }
      }
    }
    return getAttackMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getEndTurnMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "EndTurn",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getEndTurnMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getEndTurnMethod;
    if ((getEndTurnMethod = GameSessionServiceGrpc.getEndTurnMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getEndTurnMethod = GameSessionServiceGrpc.getEndTurnMethod) == null) {
          GameSessionServiceGrpc.getEndTurnMethod = getEndTurnMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "EndTurn"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("EndTurn"))
              .build();
        }
      }
    }
    return getEndTurnMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> getGetSessionStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSessionState",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> getGetSessionStateMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest, com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> getGetSessionStateMethod;
    if ((getGetSessionStateMethod = GameSessionServiceGrpc.getGetSessionStateMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getGetSessionStateMethod = GameSessionServiceGrpc.getGetSessionStateMethod) == null) {
          GameSessionServiceGrpc.getGetSessionStateMethod = getGetSessionStateMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest, com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSessionState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("GetSessionState"))
              .build();
        }
      }
    }
    return getGetSessionStateMethod;
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
    if ((getGetPlayerClassesMethod = GameSessionServiceGrpc.getGetPlayerClassesMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getGetPlayerClassesMethod = GameSessionServiceGrpc.getGetPlayerClassesMethod) == null) {
          GameSessionServiceGrpc.getGetPlayerClassesMethod = getGetPlayerClassesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPlayerClasses"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("GetPlayerClasses"))
              .build();
        }
      }
    }
    return getGetPlayerClassesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getProposePeaceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProposePeace",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getProposePeaceMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getProposePeaceMethod;
    if ((getProposePeaceMethod = GameSessionServiceGrpc.getProposePeaceMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getProposePeaceMethod = GameSessionServiceGrpc.getProposePeaceMethod) == null) {
          GameSessionServiceGrpc.getProposePeaceMethod = getProposePeaceMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProposePeace"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("ProposePeace"))
              .build();
        }
      }
    }
    return getProposePeaceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getRespondToPeaceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RespondToPeace",
      requestType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest.class,
      responseType = com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest,
      com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getRespondToPeaceMethod() {
    io.grpc.MethodDescriptor<com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> getRespondToPeaceMethod;
    if ((getRespondToPeaceMethod = GameSessionServiceGrpc.getRespondToPeaceMethod) == null) {
      synchronized (GameSessionServiceGrpc.class) {
        if ((getRespondToPeaceMethod = GameSessionServiceGrpc.getRespondToPeaceMethod) == null) {
          GameSessionServiceGrpc.getRespondToPeaceMethod = getRespondToPeaceMethod =
              io.grpc.MethodDescriptor.<com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest, com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RespondToPeace"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GameSessionServiceMethodDescriptorSupplier("RespondToPeace"))
              .build();
        }
      }
    }
    return getRespondToPeaceMethod;
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
    default void joinSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getJoinSessionMethod(), responseObserver);
    }

    /**
     */
    default void createSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateSessionMethod(), responseObserver);
    }

    /**
     */
    default void move(com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMoveMethod(), responseObserver);
    }

    /**
     */
    default void attack(com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAttackMethod(), responseObserver);
    }

    /**
     */
    default void endTurn(com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getEndTurnMethod(), responseObserver);
    }

    /**
     */
    default void getSessionState(com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetSessionStateMethod(), responseObserver);
    }

    /**
     */
    default void getPlayerClasses(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPlayerClassesMethod(), responseObserver);
    }

    /**
     */
    default void proposePeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProposePeaceMethod(), responseObserver);
    }

    /**
     */
    default void respondToPeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRespondToPeaceMethod(), responseObserver);
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
    public void joinSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getJoinSessionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateSessionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void move(com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMoveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void attack(com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAttackMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void endTurn(com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getEndTurnMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSessionState(com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetSessionStateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPlayerClasses(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPlayerClassesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void proposePeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProposePeaceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void respondToPeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest request,
        io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRespondToPeaceMethod(), getCallOptions()), request, responseObserver);
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
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse joinSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getJoinSessionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse createSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCreateSessionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse move(com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getMoveMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse attack(com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getAttackMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse endTurn(com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getEndTurnMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse getSessionState(com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetSessionStateMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse getPlayerClasses(com.google.protobuf.Empty request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetPlayerClassesMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse proposePeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getProposePeaceMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse respondToPeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getRespondToPeaceMethod(), getCallOptions(), request);
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
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse joinSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getJoinSessionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse createSession(com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateSessionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse move(com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMoveMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse attack(com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAttackMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse endTurn(com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getEndTurnMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse getSessionState(com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetSessionStateMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse getPlayerClasses(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPlayerClassesMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse proposePeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProposePeaceMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse respondToPeace(com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRespondToPeaceMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse> joinSession(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getJoinSessionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse> createSession(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateSessionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> move(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMoveMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> attack(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAttackMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> endTurn(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getEndTurnMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse> getSessionState(
        com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetSessionStateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse> getPlayerClasses(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPlayerClassesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> proposePeace(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProposePeaceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse> respondToPeace(
        com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRespondToPeaceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_JOIN_SESSION = 0;
  private static final int METHODID_CREATE_SESSION = 1;
  private static final int METHODID_MOVE = 2;
  private static final int METHODID_ATTACK = 3;
  private static final int METHODID_END_TURN = 4;
  private static final int METHODID_GET_SESSION_STATE = 5;
  private static final int METHODID_GET_PLAYER_CLASSES = 6;
  private static final int METHODID_PROPOSE_PEACE = 7;
  private static final int METHODID_RESPOND_TO_PEACE = 8;

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
          serviceImpl.joinSession((com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse>) responseObserver);
          break;
        case METHODID_CREATE_SESSION:
          serviceImpl.createSession((com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse>) responseObserver);
          break;
        case METHODID_MOVE:
          serviceImpl.move((com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>) responseObserver);
          break;
        case METHODID_ATTACK:
          serviceImpl.attack((com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>) responseObserver);
          break;
        case METHODID_END_TURN:
          serviceImpl.endTurn((com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>) responseObserver);
          break;
        case METHODID_GET_SESSION_STATE:
          serviceImpl.getSessionState((com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse>) responseObserver);
          break;
        case METHODID_GET_PLAYER_CLASSES:
          serviceImpl.getPlayerClasses((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>) responseObserver);
          break;
        case METHODID_PROPOSE_PEACE:
          serviceImpl.proposePeace((com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>) responseObserver);
          break;
        case METHODID_RESPOND_TO_PEACE:
          serviceImpl.respondToPeace((com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest) request,
              (io.grpc.stub.StreamObserver<com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>) responseObserver);
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
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcJoinResponse>(
                service, METHODID_JOIN_SESSION)))
        .addMethod(
          getCreateSessionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcCreateSessionResponse>(
                service, METHODID_CREATE_SESSION)))
        .addMethod(
          getMoveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcMoveRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>(
                service, METHODID_MOVE)))
        .addMethod(
          getAttackMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcAttackRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>(
                service, METHODID_ATTACK)))
        .addMethod(
          getEndTurnMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcEndTurnRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>(
                service, METHODID_END_TURN)))
        .addMethod(
          getGetSessionStateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GetStateRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GetStateResponse>(
                service, METHODID_GET_SESSION_STATE)))
        .addMethod(
          getGetPlayerClassesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse>(
                service, METHODID_GET_PLAYER_CLASSES)))
        .addMethod(
          getProposePeaceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcProposePeaceRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>(
                service, METHODID_PROPOSE_PEACE)))
        .addMethod(
          getRespondToPeaceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcRespondToPeaceRequest,
              com.dungeoncrawler.contracts.grpc.gamesession.GrpcActionResponse>(
                service, METHODID_RESPOND_TO_PEACE)))
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
              .addMethod(getCreateSessionMethod())
              .addMethod(getMoveMethod())
              .addMethod(getAttackMethod())
              .addMethod(getEndTurnMethod())
              .addMethod(getGetSessionStateMethod())
              .addMethod(getGetPlayerClassesMethod())
              .addMethod(getProposePeaceMethod())
              .addMethod(getRespondToPeaceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
