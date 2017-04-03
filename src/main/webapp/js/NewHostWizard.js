var NewHostWizard = function($scope, $uibModalInstance, $http, $timeout, appsession, uuid4) {
    $scope.pubkeyUptoDate = false;
    $scope.pubkeyUpdating = false;
    $scope.inprg = false;

    $scope.errors = [];

    $scope.host = {
        "@type" : 'host',
        id : uuid4.generate(),
        address : '',
        publicKey : '',
        dedicated : true
    };
    $scope.password = {
    	password: ''
    };

	$scope.usepubkey = true;

	$scope.controllerKey = '';

    $scope.updateTimeout = null;
    $scope.clearPublicKey = function (event) {
		if(event.keyCode == 13) {
			$scope.addHost();
		}
        $scope.host.publicKey = '';
    }
    $scope.errorHandler = function(error, responseCode) {
		$scope.errors = [error];
    };
    $scope.updatePubkey = function () {
    	if($scope.host.address == '') {
    		return;
    	}
        $scope.host.publicKey = '';
        $scope.pubkeyUpdating = true;
        if($scope.updateTimeout != null) {
            $timeout.cancel($scope.updateTimeout);
            $scope.pubkeyUptoDate = false;
        }
        appsession.get('s/r/host/helpers/pubkey?address='+$scope.host.address)
            .success(function(pubkey) {
                $scope.pubkeyUptoDate = true;
                $scope.pubkey = pubkey;
                $scope.host.publicKey = pubkey.fingerprint;
		        $scope.pubkeyUpdating = false;
		        $scope.errors = [];
            })
            .error(function(error) {
            	$scope.errorHandler(error);
            	$scope.pubkeyUpdating = false;
            });
    };
    $scope.close = function() {
        $uibModalInstance.dismiss('cancel');
    };
	$scope.onKeyPress = function(event) {
		if(event.keyCode == 13) {
			$scope.addHost();
		}
	};
	$scope.toggleDedicated = function() {
		$scope.host.dedicated = !$scope.host.dedicated;
	};
    $scope.addHost = function () {
    	var onHostAdded = function() {
			$uibModalInstance.close();
		};
		var hostAddError = function(error) {
			$scope.errorHandler();
			$scope.inprg = false;
		};

		$scope.inprg = true;
    	if($scope.usepubkey) {
    	    appsession.put('s/r/host/join-pubkey',$scope.host)
				.success(onHostAdded)
				.error(hostAddError);
    	} else {
			appsession.put('s/r/host/join',
				{
					host : $scope.host,
					password : $scope.password.password
				})
				.success(onHostAdded)
				.error(hostAddError);
    	}
    };

    appsession.get('s/r/host/helpers/controller-pubkey').success(function(result) {
    	$scope.controllerKey = result;
    });
};