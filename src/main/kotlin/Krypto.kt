import java.io.File
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec





class Krypto : Cli() {

    override fun handle(command: String) {
        when {
            RSA_KEY_GENERATOR in command -> handleRsaKeygen()
            RSA_ENCRYPT in command -> handleEncrypt()
            RSA_DECRYPT in command -> handleDecrypt()
            else -> super.handle(command)
        }
    }

    private fun handleDecrypt() {
        val sc = Scanner(System.`in`)

        print("Private key file name: ")
        val filename = sc.nextLine()

        val privateKeyFile = File(currentDirPath + File.separator + filename)
        if (!privateKeyFile.exists()) {
            println("File \"$filename\" does not exists")
            return
        }

        print("File to decrypt: ")
        val encryptedFileName = sc.nextLine()

        val encryptedFile = File(currentDirPath + File.separator + encryptedFileName)
        if (!encryptedFile.exists()) {
            println("File \"$encryptedFileName\" does not exists")
            return
        }

        // Достаём приватный ключ
        val keyBytes = privateKeyFile.readBytes()
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val privateRsaKey = keyFactory.generatePrivate(keySpec)

        // Расшифровываем AES-ключ
        val aesKeyFile = File(currentDirPath + File.separator + encryptedFileName.replace(".krypto", ".key"))
        if (!aesKeyFile.exists()) {
            println("File \"${encryptedFileName.replace(".krypto", ".key")}\" does not exists")
            return
        }
        val aesKeyBytes = rsaDecrypt(aesKeyFile.readBytes(), privateRsaKey)
        val aesKeySpec: SecretKey = SecretKeySpec(aesKeyBytes, "AES")


        // Расшифровываем файл
        val decryptedBytes = aesCrypto(encryptedFile.readBytes(), aesKeySpec, Cipher.DECRYPT_MODE)
        val decryptedFile = File(currentDirPath + File.separator + encryptedFileName.replace(".krypto", ""))
        decryptedFile.writeBytes(decryptedBytes)
        println("File successfully decrypted!")
    }

    private fun handleEncrypt() {
        val sc = Scanner(System.`in`)

        print("Public key file name: ")
        val filename = sc.nextLine()

        val publicKeyFile = File(currentDirPath + File.separator + filename)
        if (!publicKeyFile.exists()) {
            println("File \"$filename\" does not exists")
            return
        }

        print("File to encrypt: ")
        val encryptionFileName = sc.nextLine()
        val fileToEncrypt = File(currentDirPath + File.separator + encryptionFileName)
        if (!fileToEncrypt.exists()) {
            println("File \"$encryptionFileName\" does not exists")
            return
        }

        // Достаём публичный ключ RSA
        val keyBytes = publicKeyFile.inputStream().readBytes()
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(keyBytes)
        val key = keyFactory.generatePublic(keySpec)

        // Создаём AES ключ и шифруем с его помощью файл
        val aesKey = createAESKey()
        val encryptedFileBytes = aesCrypto(fileToEncrypt.readBytes(), aesKey, Cipher.ENCRYPT_MODE)

        // Записываем данные в файл
        File(currentDirPath + File.separator + encryptionFileName + ".krypto").apply {
            createNewFile()
            writeBytes(encryptedFileBytes)
        }

        // Шифруем AES ключ с помощью RSA и записываем его в файл
        val encryptedAes = rsaEncrypt(aesKey.encoded, key)
        File(currentDirPath + File.separator + encryptionFileName + ".key").apply {
            createNewFile()
            writeBytes(encryptedAes)
        }

        println("File successfully encrypted!")
    }

    private fun rsaDecrypt(data: ByteArray, key: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    private fun rsaEncrypt(data: ByteArray, key: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    private fun aesCrypto(data: ByteArray, key: SecretKey, mode: Int): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(mode, key)
        return cipher.doFinal(data)
    }

    private fun handleRsaKeygen() {
        val sc = Scanner(System.`in`)

        print("Key files name: ")
        val filename = sc.nextLine()

        val keys = createRSAKeys()
        keys.private.saveKeyToFile(currentDirPath, filename)
        keys.public.saveKeyToFile(currentDirPath, "$filename.pub")
        println("Keys successfully created")
    }

    private fun createAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun createRSAKeys(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.genKeyPair()
    }

    companion object {
        private const val RSA_KEY_GENERATOR = "rsa-keygen"
        private const val RSA_ENCRYPT = "rsa-encrypt"
        private const val RSA_DECRYPT = "rsa-decrypt"

    }

}

fun Key.saveKeyToFile(folderPath: String, filename: String) {
    val file = File(folderPath + File.separator + filename).apply { createNewFile() }
    file.writeBytes(encoded)
}