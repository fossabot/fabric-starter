package main

import (
	// "strconv"
	"encoding/json"
	"errors" // "strings"
	"fmt"    // "bytes"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("SimpleChaincode")

// InvoiceDataChainCode doc
type InvoiceDataChainCode struct{}

//ChainCodeFunction is a wrapper for function
type ChainCodeFunction func(shim.ChaincodeStubInterface, []string) peer.Response

//ChainCodeFunctions is a map between name of the function and actual implementation
var ChainCodeFunctions = make(map[string]ChainCodeFunction)

//Organisation -
type Organisation struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

//Contract doc
type Contract struct {
	Buyer  Organisation `json:"buyer"`
	Seller Organisation `json:"seller"`
	Factor Organisation `json:"factor"`
	ID     string       `json:"id"`
}

// Invoice is a commercial document issued by a seller to a buyer, relating to a sale transaction and indicating the products, quantities, and agreed prices for products or services the seller had provided the buyer. During factoring process Invoice is being created *after* ReceivalAdvice, because some of commodities could have been lost during transportation , and data from the Buyer are considered more trusfull. Seller or Factor are able to submit an Invoice only if the corresponding Receival Receipt had already been uploaded. The responsibility of submitter is checked against Contract with it's public key
type Invoice struct {
	ID string `json:"id"`
	// hash         string `json:"hash"`
	// shippingDate string `json:"shippingDate"`
	// totalGross   string `json:"totalGross"`
	// totalNet     string `json:"totalNet"`
	// seller       string `json:"seller"`
	// factor       string `json:"factor"`
	// buyer        string `json:"buyer"`
	// status       string `json:"status"`
	// commodities  string `json:"commodities"`
}

//InvoiceBatch is a just list of Invoices. It's only purpose is to make upload process a single trasaction instead of multiple ones.
type InvoiceBatch struct {
	Invoices []Invoice `json:"invoices"`
}

//ReceiptConfirmation  is an acknowledgement of receipt: a confirmation that a product/commodity have been received by the Buyer. Only the Buyer can submit a new ReceiptConfirmation.Role of submitting organisation is defined by it's cryptographic identity
// type ReceiptConfirmation struct {
// 	ID string `json:"id"`
// }

//ReceiptConfirmationBatch is a just list of Invoices. It's only purpose is to make upload process a single trasaction instead of multiple ones.
// type ReceiptConfirmationBatch struct {
// 	ReceiptConfirmations []ReceiptConfirmation `json:"receiptConfimations"`
// }

// Document is either Invoice or ReceiptConfirmation
type Document struct {
	ID           string      `json:"id,omitempty"`
	OrderID      string      `json:"orderId"`
	ContractID   string      `json:"contractID"`
	Hash         string      `json:"hash"`
	ShippingDate string      `json:"shippingDate"`
	TotalGross   float32     `json:"totalGross"`
	TotalNet     float32     `json:"totalNet,omitempty"`
	Buyer        string      `json:"buyer,omitempty"`
	Seller       string      `json:"seller,omitempty"`
	Factor       string      `json:"factor,omitempty"`
	DocumentType string      `json:"documentType,omitempty"`
	Commodities  []Commodity `json:"commodities"`
}
//Commodity doc
type Commodity struct {
	ID       string  `json:"id,omitempty"`
	Name     string  `json:"name"`
	Quantity float32 `json:"quantity"`
	Cost     float32 `json:"cost"`
}

//Documents doc
type Documents struct {
	Documents []Document `json:"documents"`
}

//Order is the main structure, which describes the life-cycle of shipping, receiving and paying for commodities. Status is and aggregated value just for simplicity of using in front-end
type Order struct {
	ID         string              `json:"id"`
	Buyer      string              `json:"buyer,omitempty"`
	Seller     string              `json:"seller,omitempty"`
	ContractID string              `json:"contractID"`
	StatusLog  []string            `json:"status"`
	Documents  map[string]Document `json:"documents"`
}

//Init fills the map of fuctions and corresponding names
func (t *InvoiceDataChainCode) Init(stub shim.ChaincodeStubInterface) peer.Response {

	ChainCodeFunctions["updateSingleOrder"] = updateSingleOrder
	ChainCodeFunctions["updateMultipleOrders"] = updateMultipleOrders
	ChainCodeFunctions["listOrders"] = listOrders
	ChainCodeFunctions["getOrder"] = getOrder
	ChainCodeFunctions["createContract"] = createContract
	ChainCodeFunctions["listContracts"] = listContracts
	ChainCodeFunctions["listOrdersByContract"] = listOrdersByContract
	ChainCodeFunctions["getContract"] = getContract

	return shim.Success(nil)
}

//----------------------------------------------------------------------
const (
	receiptConfirmationDocType = "receiptConfirmation"
	invoiceDocType             = "invoice"
	OrdersTable                = "Orders_v3"
	ContractsTable             = "Contracts"
)

//Invoke --
func (t *InvoiceDataChainCode) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	function, args := stub.GetFunctionAndParameters()
	logger.Noticef("%v %v", function, args)
	theFunc := ChainCodeFunctions[function]

	if theFunc != nil {
		return theFunc(stub, args)
	}
	return shim.Error("Invalid function name.")
}

// func invoiceBatch(stub shim.ChaincodeStubInterface, args []string) peer.Response {
// 	invoiceBatch := &InvoiceBatch{}
// 	foundInvoices := []Invoice{}
// 	err := json.Unmarshal([]byte(args[0]), invoiceBatch)
// 	if err != nil {
// 		logger.Errorf("Error while unmarshalling invoice: %s", args[0])
// 		logger.Errorf("Error : %v", err)
// 		return shim.Error(err.Error())
// 	}
// 	for _, invoice := range invoiceBatch.Invoices {
// 		logger.Infof("Successfully unmarshalled invoice:" + invoice.ID)
// 		foundInvoiceBytes, err := queryTableKey(stub, "invoice", invoice.ID)
// 		if err != nil {
// 			logger.Errorf("Error whilst querying: %v", err)
// 			return shim.Error(err.Error())
// 		}
// 		if foundInvoiceBytes != nil {
// 			foundInvoices = append(foundInvoices, invoice)
// 		}
// 	}
// 	invoiceBatchJSONResponseBytes, err := json.Marshal(InvoiceBatch{foundInvoices})
// 	if err != nil {
// 		logger.Errorf("Error while marshalling found invoice: %v", err)
// 		return shim.Error(err.Error())
// 	}
// 	logger.Noticef("Successfully marshalled response: %s", string(invoiceBatchJSONResponseBytes))
// 	return shim.Success(invoiceBatchJSONResponseBytes)
// }

func getOrder(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	result, err := queryTableKey(stub, OrdersTable, args[0])
	if err != nil {
		return shim.Error(err.Error())
	} else if result == nil {
		return shim.Error("Order '" + args[0] + "' does not exist")
	}
	return shim.Success(result)
}

func listOrders(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	resultsIterator, err := queryTable(stub, OrdersTable)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	buffer, err := formatAsJson(resultsIterator)
	if err != nil {
		return shim.Error(err.Error())
	}
	// creator, err := stub.GetCreator()

	return shim.Success(buffer.Bytes())
}

func listOrdersByContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	// resultsIterator, err := queryTable(stub, OrdersTable)
	resultsIterator, err := stub.GetStateByPartialCompositeKey("OrdersCompositeKey", []string{args[0]})
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	buffer, err := formatAsJson(resultsIterator)
	if err != nil {
		return shim.Error(err.Error())
	}
	// creator, err := stub.GetCreator()

	return shim.Success(buffer.Bytes())
}

func updateMultipleOrders(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	documentsBytes := []byte(args[0])
	documents := &Documents{}
	err := json.Unmarshal(documentsBytes, documents)
	if err != nil {
		logger.Errorf("Error while Unmarshalling documents")
		logger.Errorf("Error : %v", err)
		return shim.Error(err.Error())
	}
	for _, document := range documents.Documents {
		err := updateOrder(stub, document)
		if err != nil {
			logger.Errorf("Error wile processing Order %s : %v", document.OrderID, err)
			return shim.Error(err.Error())
		}
	}
	return shim.Success(nil)
}
func updateSingleOrder(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	documentBytes := []byte(args[0])
	document := &Document{}
	err := json.Unmarshal(documentBytes, document)
	if err != nil {
		logger.Errorf("Error while Unmarshalling document")
		logger.Errorf("Error : %v", err)
		return shim.Error(err.Error())
	}
	err = updateOrder(stub, *document)
	if err != nil {
		logger.Errorf("Error wile processing Order %s : %v", document.OrderID, err)
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}
func updateOrder(stub shim.ChaincodeStubInterface, document Document) error {

	updatedOrder := Order{}
	updatedOrder.Documents = make(map[string]Document)
	var updatedOrderBytes []byte
	orderID := document.OrderID
	documentType := document.DocumentType
	contractID := document.ContractID

	//Query Contract to get buyer,seller, factor and check responsibilities
	contractBytes, err := queryTableKey(stub, ContractsTable, contractID)
	if err != nil {
		logger.Errorf("Error while querying contract: %s", contractID)
		logger.Errorf("Error : %v", err)
		return err
	}
	if contractBytes == nil {
		logger.Errorf("Contract not found: %s", contractID)
		return errors.New("Contract not found: " + contractID)
	}
	contract := &Contract{}
	err = json.Unmarshal(contractBytes, contract)
	if err != nil {
		logger.Errorf("Error while unmarshalling contract: %s", contractID)
		logger.Errorf("Error : %v", err)
		return err
	}

	//TODO: add assertion for document type corresponding to roles in Contract
	// if field is empty - override it  with default responsibility from contract
	primaryKey, err := stub.CreateCompositeKey("OrdersCompositeKey", []string{contractID, orderID})
	if err != nil {
		return err
	}
	// orderQueryResultBytes, err := queryTableKey(stub, OrdersTable, orderID)
	orderQueryResultBytes, err := stub.GetState(primaryKey)
	if err != nil {
		logger.Errorf("Error while querying order: %s", orderID)
		logger.Errorf("Error : %v", err)
		return err
	}

	//If order was found, unmarshal it, check if contractId is the same and clone it to updatedOrder
	if orderQueryResultBytes != nil {
		order := &Order{}
		err := json.Unmarshal(orderQueryResultBytes, order)
		if err != nil {
			logger.Errorf("Error while Unmarshalling order: %s", orderID)
			logger.Errorf("Error : %v", err)
			return err
		}
		if order.ContractID != document.ContractID {
			return errors.New("order.ContractID = " + order.ContractID + ", document.ContractID = " + document.ContractID)
		}
		updatedOrder = *order
	} else {
		//if order is new assign Id from document
		updatedOrder.ID = orderID
		updatedOrder.ContractID = contractID
	}
	updatedOrder.StatusLog = append(updatedOrder.StatusLog, documentType)
	updatedOrder.Documents[documentType] = document

	// if err != nil {
	// 	logger.Errorf("Error while marshalling order: %s", orderID)
	// 	logger.Errorf("Error : %v", err)
	// 	return nil, "", err
	// }
	updatedOrderBytes, err = json.Marshal(updatedOrder)
	if err != nil {
		return err
	}

	// err = tablePutState(stub, OrdersTable, orderID, updatedOrderBytes)
	// 	if err != nil {
	// 		logger.Errorf("Error while saving order: %s", orderID)
	// 		logger.Errorf("Error : %v", err)
	// 		return nil, "", err
	// 	}

	// primaryKey, err = stub.CreateCompositeKey("OrdersCompositeKey", []string{contractID, orderID})
	// if err != nil {
	// 	return err
	// }
	if err = stub.PutState(primaryKey, updatedOrderBytes); err != nil {
		return err
	}
	// if err != nil {
	// 	return err
	// }
	// return ccAPI.PutState(primaryKey, data)

	return nil
}

func createContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	var contract = &Contract{}
	contractValueBytes := []byte(args[0])
	if err := json.Unmarshal(contractValueBytes, contract); err != nil {
		logger.Errorf("error while unmarshalling contract: %v", err)
		return shim.Error(err.Error())
	}
	if err := tablePutState(stub, ContractsTable, contract.ID, contractValueBytes); err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}
func listContracts(stub shim.ChaincodeStubInterface, args []string) peer.Response {

	resultsIterator, err := queryTable(stub, ContractsTable)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	buffer, err := formatAsJson(resultsIterator)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(buffer.Bytes())
}

func getContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	result, err := queryTableKey(stub, ContractsTable, args[0])
	if err != nil {
		return shim.Error(err.Error())
	} else if result == nil {
		return shim.Error("Contract '" + args[0] + "' does not exist")
	}

	return shim.Success(result)
}

// func getTestData() []string {
// 	return []string{
// 		"12345",      //id
// 		"0xasdasd",   //hash
// 		"11.01.2018", //shippingDate
// 		"100000",     //totalGross
// 		"80000",      //totalNet
// 		"12348",      //seller
// 		"12345",      //factor
// 		"12342",      //buyer
// 		"shipped",    //status
// 		"[{\"id\":\"Comm1\",\"Qty\":\"100500\"}]"}
// }

func main() {

	if err := shim.Start(new(InvoiceDataChainCode)); err != nil {
		fmt.Printf("Error starting Invoice chaincode: %s", err)
	}
}
