const req = require('request')
const options = {
    hostname: self._hostname,
    port: self._port,
    path: self._baseAPI + 'enroll',
    method: 'POST',
    auth: enrollmentID + ':' + enrollmentSecret,
    ca: self._tlsOptions.trustedRoots,
    rejectUnauthorized: self._tlsOptions.verify
};
req.post(options).then((response )=> {
    console.log(response);
})




