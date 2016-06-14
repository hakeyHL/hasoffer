package hasoffer.base.exception;

/**
 * Date : 2016/3/14
 * Function :
 */
public class ImageDownloadOrUploadException extends Exception {
    String message;

    public ImageDownloadOrUploadException() {
    }

    public ImageDownloadOrUploadException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("ImageDownloadOrUploadException{%s}", message);
    }

}
