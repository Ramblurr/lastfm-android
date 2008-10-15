package androidx.list;

public interface ListModel<T> {
  public int getCount();
  public T getItem(int position);
}
