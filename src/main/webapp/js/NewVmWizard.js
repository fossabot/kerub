var NewVmWizard = function($scope, $modalInstance, $http, $log, $timeout, appsession, uuid4, size) {
	$scope.vm = {
		"@type" : 'vm',
		id : uuid4.generate(),
		nrOfCpus : 1,
    	memory : {
    		min : size.toSize('512 MB'),
    		humanFriendlyMin : function(newMin) {
    			if(newMin) {
    				$scope.vm.memory.min = size.toSize(newMin);
    			}
    			return size.humanFriendlySize($scope.vm.memory.min);
    		},
    		max : size.toSize('1 GB'),
    		humanFriendlyMax : function(newMax) {
    			if(newMax) {
    				$scope.vm.memory.max = size.toSize(newMax);
    			}
    			return size.humanFriendlySize($scope.vm.memory.max);
    		}
    	},
    	expectations : [],
    	storagedevices : []
	};
	$scope.addVm = function() {
		appsession.put('s/r/vm', $scope.vm).success(function() {
        	$modalInstance.close();
		});
	};
    $scope.close = function() {
        $log.info('closed new vm dialog');
        $modalInstance.dismiss('cancel');
    };
}