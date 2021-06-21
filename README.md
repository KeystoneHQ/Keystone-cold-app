# Keystone-app

**The Keystone hardware wallet is simply relaunched from the Cobo Vault branding so both the code base and infrastructure are the same. For more info please checkout [here](https://blog.keyst.one/leaving-cobo-to-continue-the-cobo-vault-legacy-29bb2f8f026e)**


Keystone is an air-gapped, open source hardware wallet that uses completely transparent QR code data transmissions. Visit the [Keystone official website]( https://keyst.one)  to learn more about Keystone.

You can also follow [@Keystone](https://twitter.com/KeystoneWallet) on Twitter.

<div align=center><img src="https://keyst.one/c430c589a841d8b8379c66766e78c95d.png"/></div>

## Contents

- [Introduction](#introduction)
- [Clone](#clone)
- [Build](#build)
- [Test](#test)
- [Code Structure](#code-structure)
- [Core Dependencies](#core-dependencies)
- [License](#license)


## Introduction
Keystone runs as a standalone application on customized hardware and Android 8.1 Oreo (Go Edition).  This app performs:
1. Interaction with the user. 
2. Interaction with the mobile application [Keystone companion app](https://keyst.one/companion-app) via QR code. 
3. Interaction with the Secure Element (SE) via serial port, open source SE firmware can be found at [keystone-se-firmware](https://github.com/KeystoneHQ/keystone-se-firmware). Transaction data is signed by the Secure Element and the generated signature is sent back to the application. This signature and other necessary messages are displayed as a QR code. You can check the animation on our webpage to see the whole process. Users use their mobile or desktop application to acquire signed transaction data and broadcast it. 

The hardware wallet application was programmed with Java language. The transaction related work is done by Typescript, for which open source code is available at [crypto-coin-kit](https://github.com/KeystoneHQ/crypto-coin-kit). The J2V8 framework is used as a bridge between Java and Typescript. 


## Clone

    git clone git@github.com:KeystoneHQ/Keystone-cold-app.git --recursive

## Build
    cd Keystone-cold-app
    ./gradlew assembleVault_v2Release
You can also build with IDEs, such as `Android Studio`,`intelliJ`.

## Test
    ./gradlew test

<!-- ## Integration Guide
if you like to integrate with Keystone, checout this [intergration guide](https://github.com/KeystoneHQ/keystone-docs/blob/master/Integration_guide.md) -->

## Code Structure
Modules

`app`: Main application module

`coinlib`: Module for supported blockchains, currently included in 12 blockchains

`encryption-core`: Module for the Secure Element, includes commands, protocol, serialize/deserialize, serial port communication

## Core Dependencies
1. [crypto-coin-message-protocol](https://github.com/KeystoneHQ/keystone-crypto-coin-message-protocol) - protocol buffer of communication with the mobile application
2. [crypto-coin-kit](https://github.com/KeystoneHQ/crypto-coin-kit) - crypto-coin libraries
3. [keystone-se-firmware](https://github.com/KeystoneHQ/keystone-se-firmware) - Secure Element firmware

## License
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](https://opensource.org/licenses/)
This project is licensed under the GPL License. See the [LICENSE](LICENSE) file for details.
