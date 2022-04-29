[![Build Status](https://app.bitrise.io/app/4b686858985766f0/status.svg?token=ls19BfGu3wG58WAx6sMC7Q&branch=master)](https://app.bitrise.io/app/4b686858985766f0)
[![codecov](https://codecov.io/gh/mercadopago/px-android/branch/master/graph/badge.svg?token=ksLU5tHdUS)](https://codecov.io/gh/mercadopago/px-android)
![GitHub tag](https://img.shields.io/github/tag/mercadopago/px-android.svg)
![GitHub top language](https://img.shields.io/github/languages/top/mercadopago/px-android.svg)

#### ⚠️ PX-Android 4.53.1 is the last public release of this library ⚠️
#### ⚠️ PX-Android 4.28.0 is the last version with minimum API level 16 ⚠️

![Screenshot MercadoPago](https://i.imgur.com/ZaqavRJ.jpg)


The MercadoPago Android Payment Experience makes it easy to collect your user's credit card details inside your android app. By creating tokens, MercadoPago handles the bulk of PCI compliance by preventing sensitive card data from hitting your server.


### 🌟 Features

- Easy to install

- Easy to integrate

- PCI compliance

- Basic color customization

- Advanced color customization
 
- Lazy loading initialization support

- Custom Fragments support in certain screens

- Support to build your own Payment Processor

- Support to create your own custom Payment Method

## Installation

### Android Studio

Make sure you have the JCenter repository

    repositories {
        ...
        jcenter()
    }

Add this line to your app's `build.gradle` inside the `dependencies` section:

    implementation 'com.mercadopago.android.px:checkout:4.+'

### Local deployment

With this command you can generate a local version for testing:

    ./gradlew publishLocal

## 🐒 How to use?

Only **3** steps needed to create a basic checkout using `MercadoPagoCheckout`:

1) Import into your project
```java
import com.mercadopago.android.px.core.MercadoPagoCheckout.Builder;
```

2) Set your PublicKey and PreferenceId
```java
final MercadoPagoCheckout checkout = new MercadoPagoCheckout.Builder("public_key", "checkout_preference_id")
    .build();
```

3) Start
```java
checkout.startPayment(activityOrContext, requestCode);
```

### One line integration
```java
new MercadoPagoCheckout.Builder("public_key", "checkout_preference_id")
    .build()
    .startPayment(activityOrContext, requestCode);
```

### Credentials
Get your [Credentials](https://www.mercadopago.com.ar/developers/en/guides/faqs/credentials/)

![Screenshot Credentials](https://i.imgur.com/Oroq90g.png)

### Create your preference id
```shell
curl -X POST \
     'https://api.mercadopago.com/checkout/preferences?access_token=ACCESS_TOKEN' \
     -H 'Content-Type: application/json' \
     -d '{
           "items": [
               {
               "title": "Dummy Item",
               "description": "Multicolor Item",
               "quantity": 1,
               "currency_id": "ARS",
               "unit_price": 10.0
               }
           ],
           "payer": {
               "email": "payer@email.com"
           }
     }'
```
* payer email has to be different from the one of credentials.

### Advanced integration
Check our official code [reference](http://mercadopago.github.io/px-android/), especially ```MercadoPagoCheckoutBuilder``` object to explore all available functionalities.

### 🔮 Project Example
This project include an example project using MercadoPago PX. In case you need support contact the MercadoPago Developers Site.


## Documentation

+ [See the GitHub project.](https://github.com/mercadopago/px-android)
+ [See our Github Page's Documentation!](http://mercadopago.github.io/px-android/)
+ [Check out MercadoPago Developers Site!](http://www.mercadopago.com.ar/developers)

## Feedback

You can join the MercadoPago Developers Community on MercadoPago Developers Site:

+ [English](https://www.mercadopago.com.ar/developers/en/community/forum/)
+ [Español](https://www.mercadopago.com.ar/developers/es/community/forum/)
+ [Português](https://www.mercadopago.com.br/developers/pt/community/forum/)

## 🌈 Basic color customization

```xml

    <!-- Main color -->
    <color name="ui_components_android_color_primary">@color/your_color</color>

    <!-- Toolbar's text color -->
    <!-- Default: @color/ui_components_white_color -->
    <color name="px_toolbar_text">@color/your_color</color>

    <!-- Status Bar color -->
    <color name="ui_components_android_color_primary_dark">@color/your_color</color>

    <!-- Spinner primary color -->
    <!-- Default: @color/ui_components_android_color_primary -->
    <color name="ui_components_spinner_primary_color">@color/your_color</color>

    <!-- Spinner secondary color -->
    <!-- Default: @color/ui_components_android_color_primary -->
    <color name="ui_components_spinner_secondary_color">@color/your_color</color>

    <!-- Spinner background color -->
    <!-- Default: @color/ui_components_white_color -->
    <color name="px_background_loading">@color/your_color</color>

    <!-- Payment method icon color -->
    <!-- Default: @color/ui_components_android_color_primary -->
    <color name="px_paymentMethodTint">@color/your_color</color>

    <!-- Inputs color -->
    <!-- Default: @color/ui_components_android_color_primary -->
    <color name="px_input">@color/your_color</color>

```

Looking for something else? check here:

+ [Advanced color customization](https://github.com/mercadopago/px-android/blob/master/docs/customization.md)


## 🌈 Fonts customization

Our checkout uses REGULAR and LIGHT fonts declared here:

[Meli UI](https://github.com/mercadolibre/fury_mobile-android-ui/blob/release/5.6/ui/src/main/java/com/mercadolibre/android/ui/font/Font.java)

```
Fonts.setFonts(yourFontsPathsByType)
```

## 👨🏻‍💻 Author
Mercado Pago / Mercado Libre

## 👮🏻 License

```
MIT License

Copyright (c) 2018 - Mercado Pago / Mercado Libre

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
