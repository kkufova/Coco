/*
 * Coco the Dialogue System
 * https://github.com/kkufova/Coco
 *
 * Copyright 2018 Klara Kufova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dialog.parts;

import static dialog.constants.Speeches.*;

import java.util.Scanner;

import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Word;

public class ShippingDialog extends DialogPart {

    private static final String GRAMMAR = "shipping-dialog";

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String postcode;
    private String country;
    private String emailAddress;
    private String phoneNumber;

    private int numberOfTries = 0;

    public ShippingDialog() {
        super();
    }

    @Override
    protected boolean setUseGrammar() {
        return true;
    }

    @Override
    protected String getDialogPartGrammar() {
        return GRAMMAR;
    }

    @Override
    public void actDialogPart() throws Exception {
        synthesizeSpeech(CHECKOUT);

        chooseDelivery();
        fillInShippingInformation();
        checkShippingInformation();
    }

    private void chooseDelivery() throws Exception {
        synthesizeSpeech(CHOOSE_DELIVERY);
        Word delivery = listenForWord(Category.DELIVERIES);

        if (delivery.toString().equals("premium")) {
            synthesizeSpeech(PREMIUM_DELIVERY_COST);

            Word answer = listenForWord(Category.ANSWERS);
            if (answer.isPositive()) {
                synthesizeSpeech(TOTAL_AMOUNT_WITH_DELIVERY);
            } else {
                synthesizeSpeech(STANDARD_DELIVERY);
            }
        } else {
            synthesizeSpeech(OK);
        }
    }

    private void fillInShippingInformation() throws Exception {
        if (numberOfTries == 0) {
            synthesizeSpeech(SHIPPING_INFORMATION);
        }
        numberOfTries++;

        synthesizeSpeech(FILL_QUESTION);

        Scanner reader = new Scanner(System.in);

        synthesizeSpeech(FIRST_NAME_CORRECT);
        System.out.println("First name: " + OpeningDialog.name);
        Word answer = listenForWord(Category.ANSWERS);
        if (!answer.isPositive()) {
            synthesizeSpeech(FILL_FIRST_NAME);
            System.out.print("First name: ");
            firstName = reader.nextLine();
            synthesizeSpeech(THANK_YOU);
        } else {
            firstName = OpeningDialog.name;
            synthesizeSpeech(OK);
        }

        synthesizeSpeech(FILL_LAST_NAME);
        System.out.print("Last name: ");
        lastName = reader.nextLine();

        synthesizeSpeech(FILL_ADDRESS);
        System.out.print("Address: ");
        address = reader.nextLine();

        synthesizeSpeech(FILL_CITY);
        System.out.print("City: ");
        city = reader.nextLine();

        synthesizeSpeech(FILL_POSTCODE);
        System.out.print("Postcode: ");
        postcode = reader.nextLine();

        synthesizeSpeech(COUNTRY_CORRECT);
        System.out.println("Country: United Kingdom");
        answer = listenForWord(Category.ANSWERS);
        if (!answer.isPositive()) {
            synthesizeSpeech(FILL_COUNTRY);
            System.out.print("Country: ");
            country = reader.nextLine();
            synthesizeSpeech(THANK_YOU);
        } else {
            country = "United Kingdom";
            synthesizeSpeech(OK);
        }

        synthesizeSpeech(FILL_EMAIL);
        System.out.print("Email address: ");
        emailAddress = reader.nextLine();

        synthesizeSpeech(FILL_PHONE_NUMBER);
        synthesizeSpeech(LEAVE_EMPTY);
        System.out.print("Phone number: + ");
        phoneNumber = reader.nextLine();
        while (!phoneNumber.matches("[0-9]+")) {
            if (phoneNumber.isEmpty()) {
                break;
            }
            synthesizeSpeech(ENTER_VALID_PHONE_NUMBER);
            System.out.print("Phone number: + ");
            phoneNumber = reader.nextLine();
        }
        phoneNumber = "+".concat(phoneNumber);

        synthesizeSpeech(THANK_YOU);
    }

    private void checkShippingInformation() throws Exception {
        synthesizeSpeech(REVIEW_SHIPPING_INFO);

        String shippingInfo = "First name: " + firstName + "\n" +
                "Last name: " + lastName + "\n" +
                "Address: " + address + "\n" +
                "City: " + city + "\n" +
                "Postcode: " + postcode + "\n" +
                "Country: " + country + "\n" +
                "Email address: " + emailAddress + "\n" +
                "Phone number: " + phoneNumber;

        // Read the entered shipping information aloud if requested:
        synthesizeSpeech(SHOULD_READ_ALOUD_INFO);
        Word answer = listenForWord(Category.ANSWERS);
        if (answer.isPositive()) {
            synthesizeSpeech(ALL_RIGHT);
            synthesizeSpeech(shippingInfo);
        } else {
            System.out.println(shippingInfo);
        }

        synthesizeSpeech(EVERYTHING_CORRECT);
        answer = listenForWord(Category.ANSWERS);
        if (!answer.isPositive()) {
            synthesizeSpeech(UNFORTUNATE);
            synthesizeSpeech(FILL_SHIPPING_INFO_AGAIN);
            fillInShippingInformation();
            checkShippingInformation();
        } else {
            synthesizeSpeech(GREAT);
        }
    }

}
