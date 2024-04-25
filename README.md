# Road to 3.2

The road to 3.2 is short and worth taking


 
## SSL 
(updated April 25th, 2024)
You're going to need the right version of `curl` for this demo to work. 

If you're on a Mac, like I am, then you're going to have to install the `curl` variant with OpenSSL support, _not_ the one using the default LibreSSL.

```
brew install curl-openssl 
```

Then you're going to need to make sure that MacOS is picking up the new Homebrew build of `curl` _before_ the system-installed version! 

```
export PATH="/opt/homebrew/opt/curl/bin:$PATH"
```

then, and only then, will the test in `test.sh` work as expected.
