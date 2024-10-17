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
        // Lire l'image en tant que tableau de bytes
        BufferedImage image = ImageIO.read(inputFile);
        String dataToHide = message;

        if (encrypt) {
            dataToHide = encryptMessage(dataToHide); // Placeholder for encryption logic
        }

        // Vérifier que le message peut tenir dans l'image
        if (dataToHide.length() * 8 > image.getWidth() * image.getHeight()) {
            throw new IOException("Message trop long pour être caché dans l'image.");
        }

        int dataIndex = 0; // Index dans le message
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // Obtenir la couleur du pixel
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF; // Composante rouge
                int g = (pixel >> 8) & 0xFF;  // Composante verte
                int b = pixel & 0xFF;         // Composante bleue

                // Si nous avons encore des données à cacher
                if (dataIndex < dataToHide.length() * 8) {
                    // Obtenir le bit à cacher
                    boolean bit = (dataToHide.charAt(dataIndex / 8) >> (7 - (dataIndex % 8) ) & 1) == 1;
                    // Modifier le LSB du rouge
                    r = (r & 0xFE) | (bit ? 1 : 0);
                    dataIndex++;
                }

                // Reconstruire le pixel et le réassigner
                pixel = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, pixel);

                // Si nous avons fini d'écrire le message, sortir
                if (dataIndex >= dataToHide.length() * 8) {
                    break;
                }
            }
            if (dataIndex >= dataToHide.length() * 8) {
                break;
            }
        }

        // Écrire l'image modifiée dans le fichier de sortie
        ImageIO.write(image, "png", outputFile);
    }

    private void decodeMessage() throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        StringBuilder message = new StringBuilder();
        int dataIndex = 0; // Compteur de bits

        // Lire chaque pixel et extraire le bit de poids faible
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF; // Composante rouge

                // Extraire le LSB
                message.append((r & 1) == 1 ? '1' : '0');
                dataIndex++;

                // Si nous avons atteint 8 bits, former un caractère
                if (dataIndex % 8 == 0) {
                    int charCode = Integer.parseInt(message.substring(dataIndex - 8, dataIndex), 2);
                    if (charCode == 0) { // Arrêter si on rencontre un caractère nul
                        break;
                    }
                    message.delete(dataIndex - 8, dataIndex); // Supprimer les bits déjà utilisés
                }
            }
        }

        // Écrire le message décodé dans le fichier de sortie
        Files.write(outputFile.toPath(), message.toString().getBytes());
    }


    private String encryptMessage(String message) {
        // Placeholder for encryption logic. Implement a real encryption algorithm.
        return "ENCRYPTED(" + message + ")";
    }

    private String decryptMessage(String encryptedMessage) {
        // Placeholder for decryption logic. Implement a real decryption algorithm.
        return encryptedMessage.replace("ENCRYPTED(", "").replace(")", "");
    }
}