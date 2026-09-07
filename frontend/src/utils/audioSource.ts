export const isRemoteAudioPath = (value: string) => /^\/audiolibro\/[1-9]\d*\/audio$/.test(value);

export function isAudioSource(value: string): boolean {
  if (isRemoteAudioPath(value) || /^data:audio\/[a-z0-9.+-]+;base64,/i.test(value)) return true;
  try {
    const url = new URL(value);
    return ['http:', 'https:'].includes(url.protocol) && !url.username && !url.password;
  } catch {
    return false;
  }
}
