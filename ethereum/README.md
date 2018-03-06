# Svalinn Ethereum Wrapper

The purpose of this library is to provide easy access to the Ethereum blockchain. 

Currently the only way provided by Svalinn to interact with the Ethereum blockchain is via Json RPC. This is implemented in the ethereum-rpc library. To perform the network calls using [Retrofit](http://square.github.io/retrofit/) you can use the ethereum-rpc-retrofit library.

## Requests

To perform a request against the Ethereum blockchain create a EthRequest object and use it with the ethereum repository.


For example if you want to the balance of address `0x0000000000000000000000000000000000000000`
```
ethereumRepository
    .request(EthBalance(BigInteger.ZERO))
    .subscribe({ 
        System.out.println(it.result()) 
    })
```

These requests can also be send as batches. For this use the bulk method. 

For example if we want to get the balance of `0x0000000000000000000000000000000000000000` and `0x0000000000000000000000000000000000000001` and `0x000000000000000000000000000000000000000a`
```
ethereumRepository
    .bulk(listOf(
       EthBalance(BigInteger.ZERO, 0),
       EthBalance(BigInteger.ONE, 1),
       EthBalance(BigInteger.TEN, 2) 
    ))
    .subscribe({ 
        System.out.println(it[0].result()) 
        System.out.println(it[1].result()) 
        System.out.println(it[2].result()) 
    })
```

Make sure to use a different id for each request, else there will be unexpected results.
 