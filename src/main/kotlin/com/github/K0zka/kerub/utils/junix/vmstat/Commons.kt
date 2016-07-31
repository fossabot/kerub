package com.github.K0zka.kerub.utils.junix.vmstat

import org.apache.commons.io.input.NullInputStream
import org.apache.commons.io.output.NullOutputStream
import org.apache.sshd.client.session.ClientSession
import java.io.OutputStream

fun commonVmStat(session : ClientSession, delay : Int, out : OutputStream) {
	val exec = session.createExecChannel("vmstat ${delay}")
	exec.`in` = NullInputStream(0)
	exec.err = NullOutputStream()
	exec.out = out
	exec.open().verify()
}