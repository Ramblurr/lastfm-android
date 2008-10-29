package fm.last.rpc;

import android.content.Context;

import java.util.List;
import java.util.Map;

public interface RpcCall {
  public Map<String, String> execute(String method, List<String> params, Context context) throws Exception;
}
