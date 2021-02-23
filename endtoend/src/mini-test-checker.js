var Decimal = require('decimal.js');


var staticTests = require('./staticTests.js');

require('chai')
.use(require('chai-as-promised'));
require('chai').assert;

//How many Nodes to create
const nbNodes = 3;

var waitTill;

function waitandcheck(checkfunc, nextfunc){
	console.log("waitcheck");

	//Now do a wait check..
	setTimeout(function(){
		if(checkfunc()){
			//continue
			nextfunc();
		}else{
			//Check again..
			waitandcheck(checkfunc, nextfunc)
		}
	},1000);
}

function function1(){
	console.log("function 1");	
	
	//Do something 
	waitTill = new Date(new Date().getTime() + 5 * 1000);

	//Wait for it to finish
	waitandcheck(
		function(){
			return new Date() > waitTill;
		}, 
		function(){
			function2();		
		});
}

function function2(){
	console.log("function 2");	
	
	//Do something 
	waitTill = new Date(new Date().getTime() + 5 * 1000);

	//Wait for it to finish
	waitandcheck(
		function(){
			return new Date() > waitTill;
		}, 
		function(){
			console.log("success");		
		});
}



start_minima_testnet = function () {
    // number of nodes, list of tests
    staticTests.start_static_network_tests("star", nbNodes, function (ip_addrs) {
        
		console.log("Minima Testnet - End 2 End");


		function1();
		
        // test 0: healthcheck - are we connected?
        // curl -s 127.0.0.1:9002/status | jq '.response.connections'
        /*staticTests.run_some_tests_get(ip_addrs["1"], '/status', "", function (response) {
            response.connections.should.be.above(0);
            response.connections.should.be.equal(nbNodes-1);
		
            var dd = new Decimal("0.1"); 
	     	
	        console.log("Connections : "+response.connections+"  "+dd);	
        });*/


        /*//1. send funds with no money and assert failure
        //staticTests.run_some_tests_get(ip_addrs["1"], '/send', {"amount": 1, "address": "0xFF", "tokenid": "0x00"}, 
        staticTests.run_some_tests_get(ip_addrs["1"], '/send+1+0xFF', params="", 
            tests=function (response) {
                console.log("send response: " + JSON.stringify(response));
        });

        // 2. generate 50 coins
        staticTests.run_some_tests_get(ip_addrs["1"], '/gimme50', "", tests = function (response) {
            //        response.connections.should.be.above(0); 
            //        response.chainlength.should.be.above(1);
                    console.log("gimme50 response: " + JSON.stringify(response));
        });*/

        // 3. send funds with money
        //staticTests.run_some_tests_get(ip_addrs["1"], '/send', {"amount": 1, "address": "0xFF", "tokenid": "0x00"}, 
        /*setTimeout(function () { 
                    staticTests.run_some_tests_get(ip_addrs["1"], '/send', params="+1+0xFF", 
                        tests=function (response) {
                            console.log("send response: " + JSON.stringify(response.txpow.body.txn));
                            response.txpow.body.txn.inputs[0].amount.should.be.equal("25");
                            response.txpow.body.txn.outputs[0].amount.should.be.equal("1");
                            response.txpow.body.txn.outputs[1].amount.should.be.equal("24");
                            response.txpow.body.txn.outputs[0].address.should.be.equal("0xFF");
                    })}, 10000);
	*/


        }	
    );
}

module.exports = start_minima_testnet
