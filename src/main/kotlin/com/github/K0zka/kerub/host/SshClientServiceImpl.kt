package com.github.K0zka.kerub.host

import com.github.K0zka.kerub.utils.DefaultSshEventListener
import com.github.K0zka.kerub.utils.getLogger
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.SshException
import org.apache.sshd.common.config.keys.KeyUtils
import org.apache.sshd.common.digest.BuiltinDigests
import org.apache.sshd.common.digest.Digest
import org.apache.sshd.common.session.Session
import org.apache.sshd.common.session.SessionListener
import org.apache.sshd.common.session.helpers.AbstractSession
import org.apache.sshd.common.subsystem.sftp.SftpConstants
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.concurrent.TimeUnit

class SshClientServiceImpl(
		val client: SshClient = SshClient.setUpDefaultClient(),
		val keyPair: KeyPair,
		val maxWait: Long = 500,
		val maxWaitUnit: TimeUnit = TimeUnit.MILLISECONDS) : SshClientService {

	class ServerFingerprintChecker(val expected: String) : DefaultSshEventListener() {
		override fun sessionEvent(session: Session, event: SessionListener.Event) {
			if (SessionListener.Event.KeyEstablished == event) {
				checkServerFingerPrint(session, expected)
			}
		}
	}

	companion object {
		val logger = getLogger(SshClientServiceImpl::class)
		val digest : Digest = BuiltinDigests.md5.create()

		fun checkServerFingerPrint(session: Session, expected: String) {
			val serverKey = (session as AbstractSession).kex.serverKey
			val fingerprint = getSshFingerPrint(serverKey)
			logger.debug("checking server ssh fingerprint {}", serverKey)
			if (fingerprint != expected) {
				throw SshException("Ssh key $fingerprint does not match expected $expected ")
			}
		}
	}


	override fun installPublicKey(session: ClientSession) {
		logger.debug("{}: installing kerub public key in ssh session", session)
		session.createSftpClient().use {
			if (!it.checkFileExists(".ssh")) {
				logger.debug("{}: creating .ssh directory", session)
				it.mkdir(".ssh")
			}
			logger.debug("{}: installing public key", session)
			it.appendToFile(".ssh/authorized_keys", getPublicKey())
			val stat = it.stat(".ssh/authorized_keys")
			logger.debug("{}: setting permissions", session)
			it.setStat(".ssh/authorized_keys", stat.perms(SftpConstants.S_IRUSR or SftpConstants.S_IWUSR))
		}
		logger.debug("{}: public key installation finished", session)
	}

	fun encodePublicKey(key: RSAPublicKey): String {
		val out = ByteArrayOutputStream()

		out.write(encodeString("ssh-rsa"))
		out.write(encodeByteArray(key.publicExponent.toByteArray()))
		out.write(encodeByteArray(key.modulus.toByteArray()))

		return out.toByteArray().toBase64()
	}

	fun encodeString(str: String): ByteArray {
		val bytes = str.toByteArray(charset("ASCII"))
		return encodeByteArray(bytes)
	}

	private fun encodeByteArray(bytes: ByteArray): ByteArray {
		val out = encodeUInt32(bytes.size).copyOf(bytes.size + 4)
		bytes.forEachIndexed { idx, byte -> out[idx + 4] = byte }
		return out
	}

	fun encodeUInt32(value: Int): ByteArray {
		val bytes = ByteArray(4)
		bytes[0] = value.shr(24).and(0xff).toByte()
		bytes[1] = value.shr(16).and(0xff).toByte()
		bytes[2] = value.shr(8).and(0xff).toByte()
		bytes[3] = value.and(0xff).toByte()
		return bytes
	}

	override fun createSession(address: String, userName: String): ClientSession {
		val future = client.connect(userName, address, 22)
		future.await()
		return future.session
	}

	override fun loginWithPublicKey(address: String, userName: String, hostPublicKey: String): ClientSession {
		logger.debug("connecting to {} with public key", address)
		val future = client.connect(userName, address, 22)
		future.await()
		val session = future.session
		session.addSessionListener(ServerFingerprintChecker(hostPublicKey))
		logger.debug("sending key to {}", address)
		session.addPublicKeyIdentity(keyPair)
		logger.debug("waiting for authentication from {}", address)
		val authFuture = session.auth()
		val finished = authFuture.await(maxWait, maxWaitUnit)
		authFuture.verify()
		logger.debug("{}: Authentication finished: {} success: {}", address, finished, authFuture.isSuccess)
		return session
	}

	override fun loginWithPassword(address: String, userName: String, password: String, hostPublicKey: String): ClientSession {
		logger.debug("connecting to {} with password", address)
		val future = client.connect(userName, address, 22)
		future.await()
		val session = future.session
		session.addSessionListener(ServerFingerprintChecker(hostPublicKey))
		logger.debug("sending password {}", address)
		session.addPasswordIdentity(password)
		logger.debug("authenticating {}", address)
		val authFuture = session.auth()
		authFuture.await(maxWait, maxWaitUnit)
		authFuture.verify()
		logger.debug("authentication finished {}", address)
		return session
	}

	override fun getPublicKey(): String = """
ssh-rsa ${encodePublicKey(keyPair.public as RSAPublicKey)} #added by kerub - ${Date()}
"""

}