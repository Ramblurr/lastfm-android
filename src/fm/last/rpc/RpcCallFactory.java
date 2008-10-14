package fm.last.rpc;

import org.kxmlrpc.XmlRpcClient;

public class RpcCallFactory {

  public static RpcCall getRpcCall(String url) {
    return new XmlRpcClient(url);
  }
}
