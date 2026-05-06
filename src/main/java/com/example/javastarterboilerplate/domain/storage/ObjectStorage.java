package com.example.javastarterboilerplate.domain.storage;

/**
 * Domain contract for binary object storage as a key-value store.
 *
 * <p>The current infrastructure adapter targets S3-compatible object stores. The contract is
 * intentionally minimal: download, upload, delete. Storage keys are opaque strings; callers are
 * responsible for key naming conventions.
 */
public interface ObjectStorage {

  /**
   * Downloads a stored object by its key.
   *
   * @param key storage key; must not be {@code null} or blank
   * @return the stored object including content and metadata
   */
  StoredObject download(String key);

  /**
   * Uploads a new object or overwrites an existing one at the given key.
   *
   * @param upload upload descriptor including key, content type, bytes and metadata
   * @return the resulting stored object reflecting what was persisted
   */
  StoredObject upload(StoredObjectUpload upload);

  /**
   * Deletes the object at the given key.
   *
   * <p>This operation is idempotent: deleting a non-existent key does not throw.
   *
   * @param key storage key; must not be {@code null} or blank
   */
  void delete(String key);
}
