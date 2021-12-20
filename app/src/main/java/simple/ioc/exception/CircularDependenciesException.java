package simple.ioc.exception;

public class CircularDependenciesException extends StackOverflowError {
    public CircularDependenciesException(String error) {
        super(error);
    }
}
