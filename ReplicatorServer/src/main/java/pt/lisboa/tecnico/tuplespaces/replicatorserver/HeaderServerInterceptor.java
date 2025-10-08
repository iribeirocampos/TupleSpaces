package pt.ulisboa.tecnico.tuplespaces.replicatorserver;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Context;
import io.grpc.Contexts;

public class HeaderServerInterceptor implements ServerInterceptor {
    public static final Context.Key<String> HEADER_VALUE_CONTEXT_KEY = Context.key("delay");
    static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {

        String headerValue = requestHeaders.get(CUSTOM_HEADER_KEY);
        if (headerValue != null) {
            Context context = Context.current().withValue(HEADER_VALUE_CONTEXT_KEY, headerValue);
            return Contexts.interceptCall(context, call, requestHeaders, next);
        }
        return next.startCall(call, requestHeaders);
    }
}
