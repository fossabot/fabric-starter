package main

import (
	"fmt"
	// "encoding/json"
)

func main() {
	invoiceString := "{ \"buyer\": \"werw\", \"commodities\": [ { \"cost\": 10.0, \"id\": \"123\", \"name\": \"pipe\", \"quantity\": 3.0 }, { \"cost\": 7.0, \"id\": \"321\", \"name\": \"can\", \"quantity\": 5.0 } ], \"factor\": \"dsd\", \"hash\": \"asda\", \"id\": \"asd\", \"seller\": \"werwe\", \"shippingDate\": \"dasd\", \"status\": \"sad\", \"totalGross\": 12.0, \"totalNet\": 13.0 }"

	fmt.Printf("Invoice: %s" , invoiceString )
}
