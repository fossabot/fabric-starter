package main

import (
	"bytes"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

//----------------------------------------------------------------------
const (
	TablesCompositeKeyName string = "table-key"

	Miners      string = "miners"
	Affinages   string = "affinages"
	Banks       string = "banks"
	Contracts   string = "contracts"
	AffContract string = "affcontracts"
	Files       string = "files"
)

//----------------------------------------------------------------------
// Returns buffer with JSON array containing QueryResults
//----------------------------------------------------------------------
func formatAsJson(resultsIterator shim.StateQueryIteratorInterface) (bytes.Buffer, error) {
	var buffer bytes.Buffer
	buffer.WriteString("[")

	isFirst := true
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return buffer, err
		}
		// Add a comma before array members, suppress it for the first array member
		if !isFirst {
			buffer.WriteString(",")
		}
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		//buffer.WriteString("}")
		isFirst = false
	}
	buffer.WriteString("]")

	return buffer, nil
}

func convertToBuffer(resultsIterator shim.StateQueryIteratorInterface,
	beginList func (buffer bytes.Buffer),
	writeAsListItem func (buffer bytes.Buffer, isFirst bool, key string, value []byte),
	endList func (buffer bytes.Buffer) ) (bytes.Buffer, error) {

	var buffer bytes.Buffer
	beginList(buffer)
	isFirst := true
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return buffer, err
		}
		writeAsListItem(buffer, isFirst, queryResponse.Key, queryResponse.Value)
		isFirst = false
	}
	endList(buffer)
	return buffer, nil
}

func jsonBeginList(buffer bytes.Buffer) { buffer.WriteString("[") }
func jsonEndList(buffer bytes.Buffer) { buffer.WriteString("]") }
func jsonWriteAsListItem(buffer bytes.Buffer, isFirst bool, key string, value []byte) {
	if !isFirst {
		buffer.WriteString(",")
	}
	buffer.WriteString(string(value))
}

//----------------------------------------------------------------------
func tablePutState(ccAPI shim.ChaincodeStubInterface, tableName string, key string, data []byte) error {
	primaryKey, err := ccAPI.CreateCompositeKey(TablesCompositeKeyName, []string{tableName, key})
	if err != nil {
		return err
	}
	return ccAPI.PutState(primaryKey, data)
}

//----------------------------------------------------------------------
func queryTable(ccAPI shim.ChaincodeStubInterface, tableName string) (shim.StateQueryIteratorInterface, error) {
	return ccAPI.GetStateByPartialCompositeKey(TablesCompositeKeyName, []string{tableName})
}

//----------------------------------------------------------------------
func queryTableKey(ccAPI shim.ChaincodeStubInterface, tableName string, key string) ([]byte, error) {
	primaryKey, err := ccAPI.CreateCompositeKey(TablesCompositeKeyName, []string{tableName, key})
	if err != nil {
		return nil, err
	}
	return ccAPI.GetState(primaryKey)
}
