package hasoffer.base.exception;

/**
 * Date : 2016/1/13
 * Function :
 */
public class ImageDownloadException extends Exception {

    String message;

    public ImageDownloadException() {
    }

    public ImageDownloadException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("ImageDownloadException{%s}", message);
    }
}
