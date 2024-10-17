# Stego-CLI: Steganography Encoder/Decoder

**Stego-CLI** is a command-line tool that allows you to hide secret messages in image files or extract hidden messages from them using the steganography technique. This tool supports encoding messages into images and decoding hidden messages, with an optional encryption feature to secure the messages.

## Features

- **Encoding**: Hide a secret message within an image (PNG format).
- **Decoding**: Extract a hidden message from a stego image.
- **Encryption (Optional)**: Encrypt the message before encoding, ensuring additional security.

## Prerequisites

To build and run Stego-CLI, you will need:

- **JDK 11** or higher.
- **Apache Maven** for building the project.

## Installation

### Clone the Repository

You can clone the project from the GitHub repository using the following command:

```bash
git clone https://github.com/calystoxi/stego-cli.git
cd stego-cli
```
### Build the Project

Build the project using Maven:

```bash
mvn clean install
```
After the build completes successfully, the compiled JAR file will be located in the target/ directory.

### Running the Application

To run the Stego-CLI tool from the command line:

```bash
java -jar target/stego-cli-1.0.jar [options]
```

## Usage

### Command-Line Options

|      Option       | Description                                                                                      |        Required        |
|:------------:|--------------------------------------------------------------------------------------------------|:----------------------:|
|  `-m`, `--mode`   | Mode of operation: `encode` (to hide a message) or `decode` (to extract a hidden message).        |          Yes           |
|  `-i`, `--input`  | Input image file for encoding/decoding.                                                           |          Yes           |
| `-o`, `--output`  | Output file to store the result (image with hidden message for encoding, or message file for decoding). Default: `stega.txt` |           No           |
|    `--message`    | Message to hide in the image when encoding.                                                       | Required for encoding  |
| `-e`, `--encrypt` | Flag to encrypt the message before hiding it (default: `false`).                                  |           No           |

### Encoding Example

This command will hide the message `"This is a secret"` inside the `input.png` image and save the steganographic image as `encoded_image.png`:

```bash
java -jar target/stego-cli-1.0.jar -m encode -i input.png -o encoded_image.png --message "This is a secret" -e
```

### Output:
```bash
Message successfully encoded into encoded_image.png
```
### Decoding Example

To extract the hidden message from the encoded_image.png file and save it to decoded_message.txt, run the following command:

```bash
java -jar target/stego-cli-1.0.jar -m decode -i encoded_image.png -o decoded_message.txt
```
### Output:

```bash
Message successfully decoded to decoded_message.txt
```
### Encrypting the Message (Optional)

To encrypt a message before encoding it into the image:

```bash
java -jar target/stego-cli-1.0.jar -m encode -i input.png -o encoded_image.png --message "Secret message" -e
```
The encrypted message will be hidden within the image.
## Error Handling

If you try to encode a message without providing the --message option:
```bash
java -jar target/stego-cli-1.0.jar -m encode -i input.png -o encoded_image.png
```
### Output:
```bash
Error: --message is required for encoding
```
If the message is too long to fit inside the image:
### Output:
```bash
Error processing file: Message too long to be hidden in the image.
```
## Directory Structure

```text
stego-cli
├── src
│   └── main
│       ├── java
│       │   └── ch/heigvd/dai/StegoCLI.java
│       └── resources
├── target
│   └── stego-cli-1.0.jar
├── pom.xml
└── README.md
```
## Development

If you'd like to contribute or further develop the project:

1. **Fork the repository**: [GitHub Fork Link](https://github.com/your-username/stego-cli/fork)
2. **Clone your fork**:

```bash
git clone https://github.com/your-username/stego-cli.git
cd stego-cli
```

3. **Make your changes**: Modify the Java code under `src/main/java/ch/heigvd/dai/StegoCLI.java`.
4. **Build and test**:

```bash
    mvn clean install
```

5. **Create a Pull Request**: Once you're satisfied with your changes, open a pull request on GitHub.

