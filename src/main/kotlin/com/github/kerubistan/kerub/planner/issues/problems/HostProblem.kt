package com.github.kerubistan.kerub.planner.issues.problems

import com.github.kerubistan.kerub.model.Host

interface HostProblem : Problem {
	val host : Host
}