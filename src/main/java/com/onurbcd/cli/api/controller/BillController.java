package com.onurbcd.cli.api.controller;

/*import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.dto.bill.BillPatchDto;
import com.onurbcd.cli.dto.bill.BillSaveDto;
import com.onurbcd.cli.dto.filter.BillFilter;
import com.onurbcd.cli.service.BillService;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/bill")
public class BillController extends PrimeController<BillSaveDto, BillPatchDto, BillFilter> {

    private final BillService billService;

    public BillController(BillService billService) {
        super(billService);
        this.billService = billService;
    }

    @PostMapping(path = "/openBill", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> openBill(@Valid @RequestPart("billOpen") BillOpenDto billOpenDto,
                                         @RequestPart(value = "billDocument") MultipartFile multipartFile) {

        return getCreated(billService.openBill(billOpenDto, multipartFile));
    }

    @PutMapping(path = "/closeBill/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void closeBill(@PathVariable("id") UUID id, @Valid @RequestPart("billClose") BillCloseDto billCloseDto,
                          @RequestPart(value = "receipt") MultipartFile multipartFile) {

        billService.closeBill(id, billCloseDto, multipartFile);
    }
}*/
