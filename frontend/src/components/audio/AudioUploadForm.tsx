import { ChangeEvent, FC, FormEvent, useEffect, useRef, useState } from 'react';
import { Button, Col, Form, Row } from 'react-bootstrap';
import { MAX_AUDIO_BYTES } from '../../utils/audioFile';

export interface AudioUploadFormValues {
  title: string;
  author: string;
  description: string;
  duration: number;
  file?: File;
}

export interface AudioUploadFormProps {
  onUpload: (values: AudioUploadFormValues) => Promise<void>;
  isSubmitting?: boolean;
}

/**
 * Collects metadata and the audio file from the user before delegating the
 * upload logic to the parent component.
 */
export const AudioUploadForm: FC<AudioUploadFormProps> = ({ onUpload, isSubmitting = false }) => {
  const [values, setValues] = useState<AudioUploadFormValues>({
    title: '',
    author: '',
    description: '',
    duration: 0,
  });
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const errorRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (error) errorRef.current?.focus();
  }, [error]);

  const handleChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setValues((current) => ({
      ...current,
      [name]: name === 'duration' ? Number(value) : value,
    }));
  };

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    setValues((current) => ({ ...current, file }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (isSubmitting) return;
    setError(null);

    if (!values.file) {
      setError('Please select an audio file before uploading.');
      errorRef.current?.focus();
      return;
    }

    if (!values.title.trim()) {
      setError('The title field is required.');
      errorRef.current?.focus();
      return;
    }

    if (!values.author.trim()) {
      setError('The author field is required.');
      return;
    }
    if (!Number.isFinite(values.duration) || values.duration < 0) {
      setError('Duration must be zero or a positive number.');
      return;
    }
    if (!values.file.type.startsWith('audio/') || values.file.size === 0) {
      setError('Select a non-empty audio file.');
      return;
    }
    if (values.file.size > MAX_AUDIO_BYTES) {
      setError('Audio files must be 20 MB or smaller.');
      return;
    }
    try {
      await onUpload({
        ...values,
        title: values.title.trim(),
        author: values.author.trim(),
        description: values.description.trim(),
      });
      setValues({ title: '', author: '', description: '', duration: 0 });
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (uploadError) {
      setError(
        uploadError instanceof Error
          ? uploadError.message
          : 'Uploading the audio file failed. Please try again.',
      );
      errorRef.current?.focus();
    }
  };

  return (
    <Form onSubmit={handleSubmit} aria-describedby={error ? 'upload-error' : undefined} noValidate>
      {error && (
        <div
          ref={errorRef}
          id="upload-error"
          className="alert alert-danger"
          role="alert"
          tabIndex={-1}
        >
          {error}
        </div>
      )}
      <Row className="gy-3">
        <Col md={6}>
          <Form.Group controlId="upload-title">
            <Form.Label>Title</Form.Label>
            <Form.Control
              maxLength={255}
              name="title"
              type="text"
              value={values.title}
              onChange={handleChange}
              required
              aria-required="true"
            />
          </Form.Group>
        </Col>
        <Col md={6}>
          <Form.Group controlId="upload-author">
            <Form.Label>Author</Form.Label>
            <Form.Control
              maxLength={255}
              required
              name="author"
              type="text"
              value={values.author}
              onChange={handleChange}
            />
          </Form.Group>
        </Col>
        <Col md={12}>
          <Form.Group controlId="upload-description">
            <Form.Label>Description</Form.Label>
            <Form.Control
              maxLength={5000}
              name="description"
              as="textarea"
              rows={3}
              value={values.description}
              onChange={handleChange}
            />
          </Form.Group>
        </Col>
        <Col md={6}>
          <Form.Group controlId="upload-duration">
            <Form.Label>Duration (seconds)</Form.Label>
            <Form.Control
              name="duration"
              type="number"
              min={0}
              value={values.duration}
              onChange={handleChange}
            />
            <Form.Text>Leave at 0 to detect automatically.</Form.Text>
          </Form.Group>
        </Col>
        <Col md={6}>
          <Form.Group controlId="upload-file">
            <Form.Label>Audio file</Form.Label>
            <Form.Control
              ref={fileInputRef}
              name="file"
              type="file"
              accept="audio/*"
              onChange={handleFileChange}
              required
              aria-required="true"
            />
          </Form.Group>
        </Col>
        <Col xs={12} className="d-flex justify-content-end">
          <Button type="submit" variant="primary" disabled={isSubmitting} aria-busy={isSubmitting}>
            {isSubmitting ? 'Uploading…' : 'Upload audio'}
          </Button>
        </Col>
      </Row>
    </Form>
  );
};
