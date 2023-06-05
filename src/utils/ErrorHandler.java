package utils;

public class ErrorHandler {
    public static void processError(Exception ex) {
        System.out.printf("%s %s\n", Constants.ERROR_MSG, ex.getMessage());
        System.out.println(Constants.EXIT_MSG);
        try {
            System.in.read();

        } catch (Exception exc) {
            System.out.printf("%s %s\n", Constants.ERROR_MSG, ex.getMessage());
        }
    }
}
