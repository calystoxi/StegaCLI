package ch.heigvd.dai;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import javax.imageio.ImageIO;  // Pour lire et écrire des images
import java.awt.image.BufferedImage; // Pour manipuler les images en mémoire
import java.io.*; // Pour les opérations d'entrée/sortie
import java.nio.file.*; // Pour travailler avec les chemins de fichiers
import java.util.concurrent.Callable; // Pour implémenter l'interface Callable


@Command(name = "stego-cli", description = "Steganography Encoder/Decoder CLI",
        mixinStandardHelpOptions = true, version = "1.0")
public class StegoCLI implements Callable<Integer> {

    @Option(names = {"-m", "--mode"}, description = "Mode of operation: encode or decode", required = true)
    private String mode;

    @Option(names = {"-i", "--input"}, description = "Input file (image for encoding/decoding)", required = true)
    private File inputFile;

    @Option(names = {"-o", "--output"}, description = "Output file (image for encoding or file for decoded message)", required = false, defaultValue = "stega.txt")
    private File outputFile;

    @Option(names = {"--message"}, description = "Message to hide (for encoding only)")
    private String message;

    @Option(names = {"-e", "--encrypt"}, description = "Encrypt the message before encoding", defaultValue = "false")
    private boolean encrypt;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new StegoCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            if (mode.equalsIgnoreCase("encode")) {
                if (message == null) {
                    System.err.println("Error: --message is required for encoding");
                    return 1;
                }
                encodeMessage();
                System.out.println("Message successfully encoded into " + outputFile.getPath());
            } else if (mode.equalsIgnoreCase("decode")) {
                decodeMessage();
                System.out.println("Message successfully decoded to " + outputFile.getPath());
            } else {
                System.err.println("Invalid mode. Use 'encode' or 'decode'.");
                return 1;
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private void encodeMessage() throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        String dataToHide = message;

        if (encrypt) {
            dataToHide = encryptMessage(dataToHide);
        }

        if (dataToHide.length() * 8 > image.getWidth() * image.getHeight()) {
            throw new IOException("Message trop long pour être caché dans l'image.");
        }

        int dataIndex = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                if (dataIndex < dataToHide.length() * 8) {
                    boolean bit = (dataToHide.charAt(dataIndex / 8) >> (7 - (dataIndex % 8) ) & 1) == 1;
                    r = (r & 0xFE) | (bit ? 1 : 0);
                    dataIndex++;
                }

                pixel = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, pixel);

                if (dataIndex >= dataToHide.length() * 8) {
                    break;
                }
            }
            if (dataIndex >= dataToHide.length() * 8) {
                break;
            }
        }
        ImageIO.write(image, "png", outputFile);
    }

    private void decodeMessage() throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        StringBuilder message = new StringBuilder();
        int dataIndex = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;

                message.append((r & 1) == 1 ? '1' : '0');
                dataIndex++;

                if (dataIndex % 8 == 0) {
                    int charCode = Integer.parseInt(message.substring(dataIndex - 8, dataIndex), 2);
                    if (charCode == 0) {
                        break;
                    }
                    message.delete(dataIndex - 8, dataIndex);
                }
            }
        }
        Files.write(outputFile.toPath(), message.toString().getBytes());
    }
    private String encryptMessage(String message) {
        return "ENCRYPTED(" + message + ")";
    }
    private String decryptMessage(String encryptedMessage) {
        return encryptedMessage.replace("ENCRYPTED(", "").replace(")", "");
    }
}