package com.madrzak.zeromqsampleproject.appevent;

public interface AppEvent<T> {
    void notify(T t);
}
