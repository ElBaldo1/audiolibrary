import { AudioTrack } from '../types/audio';

// IndexedDB keeps audio out of localStorage's small, synchronous quota.
async function database(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('audiolibrary', 1);
    request.onupgradeneeded = () => request.result.createObjectStore('tracks', { keyPath: 'id' });
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(new Error('Browser storage is unavailable.'));
    request.onblocked = () => reject(new Error('Close other AudioLibrary tabs and try again.'));
  });
}

export async function readLocalTracks(): Promise<AudioTrack[]> {
  const db = await database();
  try {
    return await new Promise((resolve, reject) => {
      const request = db.transaction('tracks').objectStore('tracks').getAll();
      request.onsuccess = () => resolve(request.result as AudioTrack[]);
      request.onerror = () => reject(request.error);
    });
  } finally {
    db.close();
  }
}

export async function writeLocalTrack(track: AudioTrack): Promise<void> {
  const db = await database();
  try {
    await new Promise<void>((resolve, reject) => {
      const transaction = db.transaction('tracks', 'readwrite');
      transaction.objectStore('tracks').put(track);
      transaction.oncomplete = () => resolve();
      transaction.onabort = () =>
        reject(new Error('Not enough browser storage to save this audio.'));
      transaction.onerror = () => reject(transaction.error);
    });
  } finally {
    db.close();
  }
}
