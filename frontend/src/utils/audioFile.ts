export const MAX_AUDIO_BYTES = 20 * 1024 * 1024;

export function readAudioFile(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error('The audio file could not be read.'));
    reader.readAsDataURL(file);
  });
}

export function readDuration(file: File): Promise<number> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const audio = new Audio();
    const cleanup = () => {
      clearTimeout(timeout);
      audio.removeAttribute('src');
      URL.revokeObjectURL(url);
    };
    const timeout = setTimeout(() => {
      cleanup();
      reject(new Error('Could not read duration. Enter it manually and retry.'));
    }, 10000);
    audio.onloadedmetadata = () => {
      const duration = audio.duration;
      cleanup();
      if (Number.isFinite(duration) && duration > 0) resolve(Math.ceil(duration));
      else reject(new Error('Enter the duration manually for this file.'));
    };
    audio.onerror = () => {
      cleanup();
      reject(new Error('This browser cannot read the audio format.'));
    };
    audio.preload = 'metadata';
    audio.src = url;
  });
}
