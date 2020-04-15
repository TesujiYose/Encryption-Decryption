package encryptdecrypt;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


class EncryptSender {

    private OperationsMethod method;

    public void setMethod(OperationsMethod method) {
        this.method = method;
    }

    public String crypt(String msg, int shift) {
        return this.method.crypt(msg, shift);
    }

    public String decrypt(String msg, int shift) {
        return this.method.decrypt(msg, shift);
    }
}


interface OperationsMethod {

    String crypt(String msg, int shift);

    String decrypt(String msg, int shift);

}

class ShiftMethod implements OperationsMethod {

    String smallAlphabet = "abcdefghijklmnopqrstuvwxyz";
    String bigAlphabet = smallAlphabet.toUpperCase();
    int lenAlp = smallAlphabet.length();

    @Override
    public String crypt(String msg, int shift) {
        // System.out.println("Crypting via ShiftMethod");
        String encryptedMsg = "";

        for (int i = 0; i < msg.length(); i++) {
            String ch = String.valueOf(msg.charAt(i));
            if (ch.equals(" ") || ch.equals("!") || ch.equals(".") || ch.equals(",")) {
                encryptedMsg += msg.charAt(i);
                continue;
            }
            int index;
            if (ch.equals(ch.toUpperCase())) {
                index = bigAlphabet.indexOf(ch);
                index += shift;
                index %= lenAlp;
                encryptedMsg += bigAlphabet.charAt(index);
            } else {
                index = smallAlphabet.indexOf(ch);
                index += shift;
                index %= lenAlp;
                encryptedMsg += smallAlphabet.charAt(index);
            }
        }

        return (encryptedMsg);
    }

    @Override
    public String decrypt(String msg, int shift) {
        //System.out.println("Decrypting via ShiftMethod");
        String decryptedMsg = "";
        for (int i = 0; i < msg.length(); i++) {
            String ch = String.valueOf(msg.charAt(i));
            int index;
            if (ch.equals(" ") || ch.equals("!") || ch.equals(".") || ch.equals(",")) {
                decryptedMsg += msg.charAt(i);
                continue;
            }
            if (ch.equals(ch.toUpperCase())) {
                index = bigAlphabet.indexOf(ch);
                index -= shift;
                index %= lenAlp - 1;
                index = index < 0 ? index + lenAlp : index;
                decryptedMsg += bigAlphabet.charAt(index);
            } else {
                index = smallAlphabet.indexOf(ch);
                index -= shift;
                index %= lenAlp - 1;
                index = index < 0 ? index + lenAlp : index;
                decryptedMsg += smallAlphabet.charAt(index);
            }
        }
        return (decryptedMsg);

    }
}

class EncryptUnicode implements OperationsMethod {

    @Override
    public String crypt(String msg, int shift) {
        // System.out.println("Crypting via EncryptUnicode");
        String encryptedMsg = "";

        for (int i = 0; i < msg.length(); i++) {
            int a = ((msg.charAt(i) - ' ' + shift) % 95) + ' ';
            encryptedMsg += (char) a;
        }

        return (encryptedMsg);
    }

    @Override
    public String decrypt(String msg, int shift) {
        // System.out.println("Decrypting via EncryptUnicode");
        String decryptedMsg = "";

        for (int i = 0; i < msg.length(); i++) {
            int a = ((msg.charAt(i) - ' ' - shift) % 95) + ' ';
            decryptedMsg += (char) a;
        }

        return (decryptedMsg);
    }

}

abstract class Encrypter {
    //def params
    private String[] args;
    String alg = "shift";
    String mode = "enc";
    int key = 0;
    String data = "";
    boolean isData = false;
    boolean isOut = false;
    boolean isIn = false;
    String inPath = "";
    String outPath = "";
    String outputMsg = "";


    public Encrypter(String[] args) {
        this.args = args;
    }

    public void process() {
        readParams();
        doMagic();
        output();
    }

    public void readParams() {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-mode")) {
                mode = args[i + 1];
            }
            if (args[i].equals("-key")) {
                key = Integer.parseInt(args[i + 1]);
            }
            if (args[i].equals("-alg")) {
                alg = args[i + 1];
            }
            if (args[i].equals("-in")) {
                inPath = args[i + 1];
                isIn = true;
            }
            if (args[i].equals("-out")) {
                outPath = args[i + 1];
                isOut = true;
            }

            if (args[i].equals("-data")) {
                data = args[i + 1];
                isData = true;
            }
        }

        if (!isData && isIn) {
            data = "";
            File file = new File(inPath);
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNext()) {
                    data = data + sc.nextLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void doMagic();

    public void output() {

        if (isOut) {
            File outFile = new File(outPath);
            try (PrintWriter printWriter = new PrintWriter(outFile)) {
                printWriter.println(outputMsg); // prints a string and then terminates the line
            } catch (IOException e) {
                System.out.printf("An exception occurs %s", e.getMessage());
            }
        } else {
            System.out.println(outputMsg);
        }
    }
}

class EncryptDecrypt extends Encrypter {


    public EncryptDecrypt(String[] args) {
        super(args);
    }

    @Override
    public void doMagic() {

        EncryptSender sender = new EncryptSender();


        switch (alg) {
            case "shift":
                sender.setMethod(new ShiftMethod());
                break;
            case "unicode":
                sender.setMethod(new EncryptUnicode());
                break;
            default:
                System.out.println("Wrong -alg param");
        }

        switch (mode) {
            case "enc":
                outputMsg = sender.crypt(data, key);
                break;
            case "dec":
                outputMsg = sender.decrypt(data, key);
                break;
            default:
                System.out.println("Wrong -mode param");
        }
    }
}


public class Main {

    public static void main(String[] args) {
        Encrypter method = new EncryptDecrypt(args);
        method.process();
    }

}

