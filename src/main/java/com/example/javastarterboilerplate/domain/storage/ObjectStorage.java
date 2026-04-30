package com.example.javastarterboilerplate.domain.storage;

public interface ObjectStorage {

  StoredObject download(String key);

  StoredObject upload(StoredObjectUpload upload);

  void delete(String key);
}
