## Unreleased
_XX_03_2022_
* FIX - Parameter specified as non-null is null - parameter state

## VERSION 4.108.1
_24_03_2022_
* FIX - CheckoutActivity launch mode changed to clean activity correctly.
* FIX - Parameter specified as non-null is null - solution: avoid to clear session with a new method

## VERSION 4.107.0
_17_03_2022_
* FIX - Congrats empty texts when paying with account money
* FIX - Duplicated payments in some cases where pay button was being enabled incorrectly.
* FIX - Adjust track offline_methods.

## VERSION 4.106.0
_10_03_2022_
* FIX - Removed total duplicate when payment method is debit card.
* FIX - Call to action when payment method is debin.
* FIX - Crash payment method not found.

## VERSION 4.105.0
_24_02_2022_
* FEATURE - New wordings for IFPE regulation.

## VERSION 4.104.1
_16_02_2022_
* FIX - Crash when badge placeholder is 0.

## VERSION 4.104.0
_09_02_2022_
* FIX - T&C selection fixed when changing installments for consumer credits.
* ENHANCEMENT - Added tests for debin changes.

## VERSION 4.103.1
_16_02_2022_
* FIX - Crash when badge placeholder is 0.

## VERSION 4.103.0
_03_02_2022_
* FEATURE - Debin support added.
* ENHANCEMENT - Track for post payment flow added.

## VERSION 4.102.0
_27_01_2022_
* ENHANCEMENT - Add backUrl and redirectUrl handling for PostPaymentUrl flow.
* FIX - Consumer credits payment crashing when previous payment was with split
* FEATURE - Adds GAv2 tracker to enum

## VERSION 4.101.0
_20_01_2022_
* FEATURE - Added PostPaymentUrl flow

## VERSION 4.100.1
_13_01_2022_
* FIX - Reverted debin version feature flag

## VERSION 4.100.0
_13_01_2022_
* ENHANCEMENT - Added debin version feature flag
* ENHANCEMENT - Migrated OneTapPresenter to kotlin
* FIX - Disabled remedies back button when paying

## VERSION 4.99.1
_16_12_2021_
* ENHANCEMENT - Added px-core new module
* FIX - NPE caused from wrong parcelize of fragment state

## VERSION 4.98.0
_09_12_2021_
* ENHANCEMENT - Adds max pay button animation timeout for all flows
* ENHANCEMENT - Adds flag to track wrapper to avoid tracking experiments label

## VERSION 4.97.1
_07_12_2021_
* FIX - Pay button pressed track crash with destroy activities

## VERSION 4.97.0
_02_12_2021_
* FIX - Charge zero showing on summary
* FIX - Missing payment configuration making pay button explode infinite

## VERSION 4.96.2
_25_11_2021_
* FIX - NullPointerException is fixed when the checkout is started by onNewIntent.

## VERSION 4.96.1
_18_11_2021_
* FIX - Payment result screen fixed

## VERSION 4.96.0
_11_11_2021_
* FIX - Fixed broken CVV animation
* FIX - Changed discount info icon
* ENHANCEMENT - Added checkout type specifications
* ENHANCEMENT - Added track for pay button click

## VERSION 4.95.0
_04_11_2021_
* FIX - Legacy flowId remaining in memory between flows

## VERSION 4.94.0
_21_10_2021_
* FIX - Network error
* ENHANCEMENT - Feature flag for threeds sdk version

## VERSION 4.93.0
_14_10_2021_
* FIX - Duplicated custom charges on IDC
* ENHANCEMENT - Remedies screen adjustments

## VERSION 4.92.1
_05_10_2021_
* FIX - Summary nav bar arrow color

## VERSION 4.92.0
_30_09_2021_
* FEATURE - Summary and offline methods restyling
* FEATURE - Improvements in remedies screen and modal implementation when offering consumer credits

## VERSION 4.91.0
_27_09_2021_
* FEATURE - Support taxable charges and label customization

## VERSION 4.90.0
_23_09_2021_
* FEATURE - Support custom scheduled payments.

## VERSION 4.89.0
_09_09_2021_
* FEATURE - Migrates to min api 21 and target api 30.
* FEATURE - Sends access token in header instead of query param.
* ENHANCEMENT - Uses card bottom label from card drawer.
* FIX - Bug in TyC url.
* FIX - New card id in body instead of query param.

## VERSION 4.88.2
_07_09_2021_
* FIX - Changes payment congrats type mapper to use decorator.

## VERSION 4.88.1
_27_08_2021_
* FIX - Connection check with VPN in API < 29.

## VERSION 4.87.2
_27_08_2021_
* FIX - Connection check with VPN in API < 29.

## VERSION 4.88.0
_26_08_2021_
* FEATURE - Migration of Nudata flow to Dynamic Features also update PXBehaviourConfigurer to avoid null sets.
* FEATURE - Adds notification message in congrats for MLA when using third party cards.

## VERSION 4.87.1
_25_08_2021_
* FIX - Crash on congrats with split payments

## VERSION 4.87.0
_19_08_2021_
* FEATURE - Added profile id as header in payments api call

## VERSION 4.86.0
_12_08_2021_
* FEATURE - Added support for IDC regulation
* FIX - Fix resource not found exception

## VERSION 4.85.1
_02_08_2021_
* FIX - Sends correct product id on discount params.

## VERSION 4.85.0
_28_07_2021_
* FEATURE - Adds Accept third party card flag.
* ENHANCEMENT - Process charges in backend.
* ENHANCEMENT - Removes cardFound retry after associating a card.
* ENHANCEMENT - Migrates to coroutines in checkout repository.

## VERSION 4.84.0
_15_07_2021_
* FIX - Crash on restoring one tap after a error.
* FIX - Exit transition on back pressed.

## VERSION 4.83.0
_08_07_2021_
* ENHANCEMENT - Sends paymentTypeId to congrats endpoint.

## VERSION 4.82.0
_24_06_2021_
* ENHANCEMENT - Adds device security flag in all tracks.

## VERSION 4.81.1
_11_06_2021_
* FIX - BusinessResult crash.

## VERSION 4.81.0
_10_06_2021_
* FEATURE - Added PIX payment method.

## VERSION 4.80.0
_03_06_2021_
* FIX - Application selection by one tap item.
* ENHANCEMENT - Added feature flag provider.

## VERSION 4.79.0
_27_05_2021_
* FIX - Applications track collided with Melidata application.
* FIX - Audio player crash when api call failed.
* ENHANCEMENT - Added long retry for special cases.

## VERSION 4.78.0
_20_05_2021_
* FEATURE - Installments v2.1

## VERSION 4.77.1
_13_05_2021_
* FIX - Reverted Minimum API level to 19.

## VERSION 4.77.0
_13_05_2021_
* FEATURE - Minimum API level 21.
* FIX - Locale interceptor.

## VERSION 4.75.2
_21_04_2021_
* FIX - Crash serializing complex map keys.
* FIX - Crash disabling lowres credits card.
* FIX - Incorrect warning on offline methods.
* ENHANCEMENT - Migrates from Bintray to Nexus.

## VERSION 4.76.1
_20_04_2021_
* FIX - Crash serializing complex map keys.
* FIX - Crash disabling lowres credits card.
* FIX - Incorrect warning on offline methods.

## VERSION 4.76.0
_15_04_2021_
* FEATURE - Enables combo card.
* FEATURE - Retries STP tokenization in case it's needed.
* ENHANCEMENT - Migrates from Bintray to Nexus.
* ENHANCEMENT - Changed CHA package.
* FIX - Shows correct style for hybrid card.

## VERSION 4.75.1
_13_04_2021_
* FIX - Offline methods were broken in last release.
* FIX - Disabled methods in remedies.

## VERSION 4.75.0
_08_04_2021_
* FEATURE - Hybrid card for MLB.
* FEATURE - Combo card (turned off by default for now)

## VERSION 4.74.0
_06_04_2021_
* FEATURE - ThreeDSBehaviour added

## VERSION 4.73.1
_30_03_2021_
* FIX - Retry build to address card form signature problem.
* ENHANCEMENT - Adds some pay button tests.

## VERSION 4.72.2
_30_03_2021_
* FIX - Retry build to address card form signature problem.

## VERSION 4.73.0
_25_03_2021_
* FEATURE - Sonic branding.

## VERSION 4.72.1
_18_03_2021_
* FIX - Crash tracking session halted.

## VERSION 4.72.0
_11_03_2021_
* ENHANCEMENT - Added validation program id to checkout data.

## VERSION 4.71.0
_03_03_2021_
* ENHANCEMENT - Migrated to Gradle 6.7.1 and AGP 4.0.1.
* ENHANCEMENT - Deprecated locale behaviour.
* ENHANCEMENT - Deprecated init cache in favor of separated repositories.
* ENHANCEMENT - Removed deprecated validators from the testlib.
* ENHANCEMENT - Tracker dependency injection.

## VERSION 4.70.0
_28_01_2021_
* FEATURE - Generate jacoco coverage report
* ENHANCEMENT - Migrated to Gradle 6.6
* FIX - Fix session clear in prefetch service
* FIX - Avoiding exploding fragment context crash

## VERSION 4.69.0
_14_01_2021_
* FEATURE - Card Drawer for AM
* FEATURE - Card tap modal behaviour
* FIX - ViewPager height zero in some extreme cases

## VERSION 4.68.0
_07_01_2021_
* ENHANCEMENT - Added merchantOrderId to congrats api call
* FEATURE - Added WebPay flow for debit card in Chile.

## VERSION 4.67.1
_23_12_2020_
* FIX - Removed remedies api call with business payments

## VERSION 4.67.0
_18_12_2020_
* ENHANCEMENT - Removed old checkout flow.

## VERSION 4.66.0
_10_12_2020_
* FEATURE - Added Loyalty Broadcaster in Payment Congrats
* ENHANCEMENT - Added Discount Center track factory
* FIX - Added missing "remedies" to business congrats error track
* FIX - Security code reason track

## VERSION 4.65.0
_03_12_2020_
* FEATURE - Appending params to back_url and redirect_url

## VERSION 4.64.0
_26_11_2020_
* FEATURE - Added support to cards without cvv.
* ENHANCEMENT - Added location granted status in congrats endpoint.

## VERSION 4.63.0
_12_11_2020_
* FEATURE - Installments v2 experiment.
* FEATURE - Improved congrats auto return experience.
* ENHANCEMENT - Added experiment seed getter into tracking behaviour.
* FIX - Prevent pay button to be enabled without a usable slider option.
* FIX - Incorrectly tracking Business congrats on view stop.
* FIX - Added last four digits to token when esc is disabled.

## VERSION 4.62.1
_06_11_2020_
* FIX - Restored congrats methods signature that caused crashes for other teams.
* FIX - Payment data extra info on result track.

## VERSION 4.62.0
_05_11_2020_
* FEATURE - Dynamic discounts params.
* FIX - Wrong payment status code in result track.
* FIX - Changed preference amount field track to total amount.

## VERSION 4.61.0
_22_10_2020_
* FEATURE - New security code screen.
* FEATURE - Add synced android id getter in esc behaviour.
* ENHANCEMENT - Throw error screen instead of exception in one tap.
* FIX - Persistent data from advanced configuration.
* FIX - Clean refresh after new card flow with recovered session.

## VERSION 4.60.0
_15_10_2020_
* FEATURE - Sending merchantOrderId to payments request body
* FIX - Handle lost liveData on recover session.

## VERSION 4.59.5
_14_10_2020_
* FIX - Avoid session clear when showing payment congrats within checkout

## VERSION 4.59.4
_30_09_2020_
* FIX - PaymentsCongrats discount info not showing.

## VERSION 4.59.3
_25_09_2020_
* FIX - PaymentCongrats tracks path with standalone flow.

## VERSION 4.59.2
_24_09_2020_
* FIX - PaymentCongrats crash with "in process" payments.
* FIX - PaymentCongrats crash with unknown payment types.
* FIX - Session not cleared on non checkout flows.

## VERSION 4.59.1
_17_09_2020_
* ENHANCEMENT - Improved offline methods bottom sheet opening.
* FIX - Check if view is attached in some cases.
* FIX - Cleared payer cost selection properly.

## VERSION 4.59.0
_15_09_2020_
* FEATURE - Standalone PaymentCongrats flow.
* ENHANCEMENT - Replaced Event with SingleLiveData.
* FIX - PostPaymentUrls are now serializable.
* FIX - Another ExplodingFragment crash.
* FIX - Avoid null PaymentConfiguration.
* FIX - Back handler when fragment is not added.

## VERSION 4.58.0
_03_09_2020_
* ENHANCEMENT - Added PaymentService and PayButtonViewModel unit tests.
* ENHANCEMENT - Moved remaining assets to ODR.
* FIX - ExplodingFragment related crashes.

## VERSION 4.57.1
_27_08_2020_
* FIX - Message for Snackbar is null.

## VERSION 4.57.0
_27_08_2020_
* FEATURE - Added AndesUI lib snackbar.
* ENHANCEMENT - Removed session init check.
* ENHANCEMENT - Legacy flow data is persisted.

## VERSION 4.56.1
_21_08_2020_
* FIX - bottom sheet fake footer height adjustment

## VERSION 4.56.0
_20_08_2020_
* FEATURE - Migrated to AndroidX and API 29.
* FEATURE - Autoselect offline methods when no other methods are available.
* FEATURE - Added support to redirect_urls and back_urls from Preference item.

## VERSION 4.55.1
_20_08_2020_
* FIX - Consumer credit file updated with dynamic suffix

## VERSION 4.55.0
_13_08_2020_
* FEATURE - Added support to shrink strict mode
* ENHANCEMENT - Added InterestRateFormatter

## VERSION 4.54.0
_05_08_2020_
* FEATURE - Added flowName configuration from preference
* ENHANCEMENT - Configure tracking params in checkout builder
* ENHANCEMENT - Added payment method information in result view track
* ENHANCEMENT - Changed deploy script

## VERSION 4.53.1
_03_08_2020_
* FIX - Added keep rules for renderer's contructor in R8 fullmode

## VERSION 4.53.0
_30_07_2020_
* ENHANCEMENT - Deprecated old groups' api
* FIX - Exploding button listeners with destroy activity
* FIX - Exploding button position with destroy activity
* FIX - pay button not hidden when exploding with destroy activity

## VERSION 4.52.0
_23_07_2020_
* ENHANCEMENT - Better discount info.
* FIX - Crash tracking fix by concurrency.
* FIX - Friction track fixed to accept a nullable card object.
* FIX - Fix bad parcelable in payment processor mapper.

## VERSION 4.51.1
_14_07_2020_
* FIX - Re-added session check due to integrator's bad implementation of SplitPaymentProcessor
* FIX - Re-added session check in fragment due to integrator's bad implementation of SplitPaymentProcessor
* ENHANCEMENT - Improved friction tracking to differentiate new frictions

## VERSION 4.50.4
_13_07_2020_
* FIX - Re-added session check in fragment due to integrator's bad implementation of SplitPaymentProcessor

## VERSION 4.50.3
_13_07_2020_
* FIX - Re-added session check due to integrator's bad implementation of SplitPaymentProcessor

## VERSION 4.51.0
_08_07_2020_
* FEATURE - Added tracking behaviour to customize tracking from application
* FIX - fixed CongratsDeepLink track path

## VERSION 4.50.2
_01_07_2020_
* FIX - No token in remedies after guessing card flow

## VERSION 4.50.1
_01_07_2020_
* FIX - Last four digits ends with null
* FIX - Prevent back when pay button is exploding
* FIX - Cloning token when no token is present (remedies)
* FIX - GSON Concurrent modification because bad cache handling

## VERSION 4.50.0
_25_06_2020_
* FEATURE - Money split view in congrats
* ENHANCEMENT - Remedies tracking changes

## VERSION 4.48.6
_24_06_2020_
* FIX - Avoid to ask for cvv preventively on groups flow

## VERSION 4.48.5
_24_06_2020_
* FIX - Avoid clear token for old card association flow

## VERSION 4.48.4
_24_06_2020_
* FIX - Clear token on prepare payment

## VERSION 4.48.3
_24_06_2020_
* FIX - DisplayInfo within PaymentMethod was not parcelized

## VERSION 4.49.1
_18_06_2020_
* FIX - Moved module extensions to avoid clashing

## VERSION 4.49.0
_18_06_2020_
* FEATURE - Added tracking behaviour new signature.
* ENHANCEMENT - Removed session initialization check

## VERSION 4.48.2
_17_06_2020_
* FIX - Crash silverbullet remedy

## VERSION 4.47.1
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.48.1
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.46.4
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.48.0
_11_06_2020_
* FEATURE - experiments with installments row
* ENHANCEMENT - persist payment configuration
* FIX - top fragment position in default congrats order

## VERSION 4.47.0
_04_06_2020_
* FEATURE - Send security type of payment as header of payment wrapper api call
* ENHANCEMENT - Alternative order in congrats
* ENHANCEMENT - Added amount to security validation data
* ENHANCEMENT - Change account money logo
* ENHANCEMENT - Take security required parameter from biometrics lib callback

## VERSION 4.46.3
_03_06_2020_
* FIX - Crash on review and confirm

## VERSION 4.45.3
_03_06_2020_
* FIX - Crash on review and confirm

## VERSION 4.46.2
_29_05_2020_
* FIX - Flow id lost on session init

## VERSION 4.45.2
_29_05_2020_
* FIX - Flow id lost on session init

## VERSION 4.46.1
_28_05_2020_
* FIX - Payment method malformed in payment data

## VERSION 4.45.1
_28_05_2020_
* FIX - Payment method malformed in payment data

## VERSION 4.46.0
_28_05_2020_
* FEATURE - Accessibility support on congrats screen
* ENHANCEMENT - Removed execute network requests to avoid bad usage
* ENHANCEMENT - Accept language interceptor
* FIX - Exploding fragment button alignment
* FIX - Crash trying to remove fragments immediately

## VERSION 4.45.0
_21_05_2020_
* FEATURE - TouchPointView in congrats.
* FEATURE - Security Type flag on payment processor
* ENHANCEMENT - Pay button fragment on review and confirm screen
* FIX - Back button in one tap covered with logo

## VERSION 4.44.0
_14_05_2020_
* FEATURE - Support for Caixa virtual payment method
* ENHANCEMENT - Pay button fragment refactor
* ENHANCEMENT - Remedy tracking data from backend
* FIX - Double init crashes with lazy init
* FIX - Business congrats not showing after remedy

## VERSION 4.43.0
_07_05_2020_
* ENHANCEMENT - Flow id header
* ENHANCEMENT - Fake locale behaviour on sample app
* FIX - Avoid summary refresh with empty charges
* FIX - Empty view on review and confirm

## VERSION 4.42.0
_30_04_2020_
* FEATURE - New suggested payment method remedy
* ENHANCEMENT - Tracking session expired friction

## VERSION 4.41.0
_23_04_2020_
* ENHANCEMENT - Added meli deeplink to one tap

## VERSION 4.40.1
_17_04_2020_
* FIX - Disable pay button in pure offline method node

## VERSION 4.39.2
_17_04_2020_
* FIX - Disable pay button in pure offline method node

## VERSION 4.40.0
_16_04_2020_
* FEATURE - Remote resources for card assets
* FEATURE - Support for IFPE regulation
* ENHANCEMENT - Support for Picasso v2.7+
* FIX - Lowres was not applying correctly
* FIX - Crash tinting status bar after paying

## VERSION 4.39.1
_08_04_2020_
* FIX - Sec code length null after invalid esc

## VERSION 4.39.0
_02_04_2020_
* FEATURE - Accessibility functions.
* FEATURE - High risk remedy.
* FIX - Crash in offline methods fragment.
* FIX - Title color in offline methods fragment.

## VERSION 4.38.2
_30_03_2020_
* FIX - Pay button crash when session isn't initialized
* FIX - Prevent crash when font configurer isn't properly initialized
* FIX - Prevent Linkable text crash
* FIX - Last four digits were null after the new cvv remedy

## VERSION 4.38.1
_21_03_2020_
* FIX - Fixed crash on business congrats
* FIX - Fixed congrats layout on instructions and body error
* FIX - Fixed pay button disappearing in one tap when returning from card form

## VERSION 4.38.0
_19_03_2020_
* FEATURE - New security code remedy
* ENHANCEMENT - Generic payment button
* FIX - NPE in card form
* FIX - Sending session id on card form

## VERSION 4.37.0
_28_02_2020_
* ENHANCEMENT - Reset esc cap when user enter security code
* ENHANCEMENT - Exit with custom error code when session is irrecoverable

## VERSION 4.36.0
_28_02_2020_
* ENHANCEMENT - Adapt new signature of ESC Manager in PX Addons
* ENHANCEMENT - Use new signature to improve delete ESC Tracking

## VERSION 4.35.4
_27_02_2020_
* ENHANCEMENT - Removed card vault dependency in one tap.
* ENHANCEMENT - Summary layout changes.
* ENHANCEMENT - ESC blacklisted payment status comes from backend.
* ENHANCEMENT - Better CVV reason tracking.
* FIX - Card form animation with split payment.

## VERSION 4.35.3
_20_02_2020_
* FIX - Restored deleted colors used by Money In

## VERSION 4.35.2
_20_02_2020_
* ENHANCEMENT - Removed button to change payer information
* FIX - Exploding color for business payments
* FIX - Low res for offline payment methods in one tap

## VERSION 4.35.0
_13_02_2020_
* FEATURE - Credits custom terms by installment.
* FEATURE - Ignoring esc flag. Now always enabled.
* ENHANCEMENT - Business components tracker.
* ENHANCEMENT - Conversion tracker.
* FIX - View not attached call propagation.

## VERSION 4.34.0
_06_02_2020_
* FEATURE - Off Methods in One Tap
* FEATURE - Esc validation before payment
* ENHANCEMENT - Check business payment decorator on payment rewards

## VERSION 4.33.1
_29_01_2020_
* FIX - Added header for terms and conditions
* FIX - Reduce margins of MLBusinessDiscountBoxView in congrats.

## VERSION 4.33.0
_24_01_2020_
* FEATURE - Added local behaviour
* ENHANCEMENT - Summary detail animation on card form return
* FIX - Sold out discount message alignment

## VERSION 4.32.0
_16_01_2020_
* FEATURE - New card form

## VERSION 4.31.0
_09_01_2020_
* ENHANCEMENT - Added tracking behaviour to addons

## VERSION 4.30.0
_09_01_2020_
* FEATURE - Added babel for translations
* FEATURE - Redesign rejected and pending congrats
* FEATURE - Backend driven discount unavailability information

## VERSION 4.29.4
_02_01_2020_
* ENHANCEMENT - Changed debit payment methods assets
* ENHANCEMENT - Compressed images
* FIX - Validate view attached on api callback
* FIX - Disable pay button when validating user biometrics (Review and Confirm)

## VERSION 4.29.3
_19_12_2019_
* FIX - Disable pay button when validating user biometrics to prevent double payments if validation is delayed

## VERSION 4.29.2
_12_12_2019_
* ENHANCEMENT - Removed backend driven texts
* ENHANCEMENT - Improved logic for redraw disabled payment methods
* FIX - Send locale toLanguageTag for Accept-Language in api call headers
* FIX - Show one tap payment method highlight if current payer cost selected has benefits

## VERSION 4.29.1
_06_12_2019_
* FIX - benefit message building for max installments 1 flows

## VERSION 4.29.0
_05_12_2019_
* FEATURE - Benefits one tap flow
* ENHANCEMENT - Upgraded to Android min API level 19
* ENHANCEMENT - Upgraded to Meli card drawer 2

## VERSION 4.28.0
_28_11_2019_
* FEATURE - Improved one tap flow
* ENHANCEMENT - Added Order and Merchant Order Id into checkout preference
* ENHANCEMENT - Added flow name to payment rewards call
* ENHANCEMENT - Upgraded to meli ui 8
* FIX - Fixed some congrats font sizes and styles

## VERSION 4.27.1
_14_11_2019_
* ENHANCEMENT - Added upward compatibility with picasso 2.7
* FIX - Parsing string to long with parse long
* FIX - Added Serializable to new models
* FIX - Remove old fragments on new intent

## VERSION 4.27.0
_07_11_2019_
* FEATURE - New endpoint "checkout" integration in replace of "payment_method"
* FEATURE - Tracking experiments data
* FIX - Refactored card token handling in session to avoid payments with cvv null

## VERSION 4.26.1
_05_11_2019_
* FIX - Validate presenter state in fragment's onDetach event

## VERSION 4.25.3
_06_11_2019_
* FIX - Validate presenter state in fragment's onDetach event

## VERSION 4.26.0
_31_10_2019_
* IMPROVEMENT - Removed try-catches from activities/fragments creation

## VERSION 4.25.2
_22_10_2019_
* FIX - Rotation bug in one tap using postCreate
* FIX - Card association flow
* FIX - Removed non mandatory allowBackup from manifest

## VERSION 4.25.1
_17_10_2019_
* FIX - Restored deleted colors used by Money In

## VERSION 4.25.0
_17_10_2019_
* ENHANCEMENT - Dynamic status bar color
* ENHANCEMENT - Refactor json util
* ENHANCEMENT - Improved timeout handling
* FIX - Prevent crashes related to session

## VERSION 4.24.0
_10_10_2019_
* ENHANCEMENT - Refactored fonts usage
* ENHANCEMENT - refactored utils
* FIX - Fixed margins in congrats components

## VERSION 4.23.1
_02_10_2019_
* FEATURE - Added receiptIdList in BusinessPayment
* ENHANCEMENT - Using safe intent for cross selling actions
* ENHANCEMENT - Fixed inconsistent empty tracking values

## VERSION 4.23.0
_02_10_2019_
* FEATURE - Added rules sets to validate when to ask for biometrics or not
* ENHANCEMENT - Added screen density interceptor to add headers in api calls
* FIX - Added single click listener to prevent fast double clicks in views

## VERSION 4.22.0
_26_09_2019_
* FEATURE - New congrats design
* FEATURE - Business components in congrats
* FIX - Friction events
* FIX - Animation on detached view

## VERSION 4.21.0
_04_09_2019_
* FEATURE - Added secure payments behaviour
* FEATURE - Added important fragment in business payment
* ENHANCEMENT - Enable TLS 1.2 in HttpClient
* ENHANCEMENT - Added collectorId to CheckoutPreference
* FIX - Security code validation
* FIX - Strict mode warning

## VERSION 4.20.0
_29_08_2019_
* FEATURE - Added tracking of session time and checkout type.
* FEATURE - Added card drawer on express payment.
* FIX - CPF validation.

## VERSION 4.19.0
_22_08_2019_
* FEATURE - Added tracking of payment method selected index and available methods quantity.
* FEATURE - Unified product id setters.
* FIX - Consumer credits crash on low res.
* FIX - Payment method discount on one tap.
* ENHANCEMENT - Gradle task for local builds.
* ENHANCEMENT - Security Code screen refactor.

## VERSION 4.18.0
_01_08_2019_
* FIX - Prevent double click on Confirm Payment.
* FIX - Changed launch mode to singleTop to prevent double initialization.
* ENHANCEMENT - Migrated deploying to Bitrise.
* ENHANCEMENT - Updated Card Association ETEs.

## VERSION 4.17.1
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.16.4
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.17.0
_18_07_2019_
* FEATURE - Changed preference mail validator to required if no private key is setted
* ENHANCEMENT - Removed payer from init track for security reasons

## VERSION 4.16.3
_16_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.15.2
_15_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.16.2
_11_07_2019_
* FIX - Default marketplace to NONE

## VERSION 4.16.1
_10_07_2019_
* FIX - Handling context in http client interceptor
* FIX - Validating flow in onRestore validations in guessing card activity

## VERSION 4.16.0
_01_07_2019_
* Added support for max installments
* Custom product id header
* Default Payment Processor for black labeled payments
* Added support for consumer credits payment method

## VERSION 4.15.1
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.14.3
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.15.0
_14_06_2019_
* FEATURE - Added gateway mode support

## VERSION 4.14.2
_14_06_2019_
* ENHANCEMENT - Now express presenter saves the current payment method index

## VERSION 4.14.1
_13_06_2019_
* FIX - Added back deprecated method

## VERSION 4.14.0
_11_06_2019_
* FEATURE - Custom pay button text support
* FEATURE - BackHandler interface for visual Payment Processor
* ENHANCEMENT - Better error handling in Express Payment
* ENHANCEMENT - CheckoutActivity MVP refactor
* ENHANCEMENT - Constants refactor
* FIX - Congrats layout

## VERSION 4.12.6
_11_06_2019_
* Fix invalid tracks again
* Fix circle reveal animation crash on Android 6

## VERSION 4.13.0
_03_06_2019_
* FEATURE - Express Payment Subtitle
* FEATURE - Express Payment Charges
* FEATURE - Modified Processing Mode
* FEATURE - New agreement params for Payer Cost
* ENHANCEMENT - Dynamic Low res in Express Payment
* ENHANCEMENT - Context for Session using a Content Provider
* ENHANCEMENT - Added view tags for ETEs
* ENHANCEMENT - IPaymentDescriptor default methods
* ENHANCEMENT - Tracking cards ESC
* ENHANCEMENT - Payer Information ETEs refactor
* ENHANCEMENT - Migration to API 28
* FIX - Payer Information restore instance

## VERSION 4.12.5
_23_05_2019_
* Fix invalid tracks

## VERSION 4.12.4
_20_05_2019_
* Fix automatic selection when the only payment method isn't card
* Changed item validation
* Fix serialization of payer lastName and firstName
* Fix amount descriptor view crop

## VERSION 4.12.3
_13_05_2019_
* FIX - Business result crash when session isn't initialized correctly

## VERSION 4.12.2
_08_05_2019_
* FIX - Removed empty box and divider in rejected views
* FIX - Catch NPE in network status check

## VERSION 4.12.1
_07_05_2019_
* FIX - Populate card properties with custom options in one tap

## VERSION 4.12.0
_03_05_2019_
* FEATURE - Esc for guessing card
* FEATURE - Using discount name for his description
* ENHANCEMENT - Track friction event when invalid esc
* ENHANCEMENT - Add reason to cvv tracker
* FIX - Returning to one tap after esc recover
* FIX - Orientation issues

## VERSION 4.11.0
_26_04_2019_
* FEATURE - Congrats tracking new attributes
* FIX - Crash payment processor activity

## VERSION 4.10.3
_25_04_2019_
* FIX - Recover removed method avoiding breaking changes.

## VERSION 4.10.2
_25_04_2019_
* FIX - Creation of session id for tracking purpose

## VERSION 4.10.1
_22_04_2019_
* ENHANCEMENT - Added amount on call for auth rejection message
* FIX - User wants to split selection persist
* FIX - Correct amount on congrats
* FIX - Reset payment method slider position on payment method changed after rejection
* FIX - Show result using visual payment processor

## VERSION 4.10.0
_11_04_2019_
* FEATURE - Disable last selected payment method after reject and recover
* FEATURE - Express Payment's support for Single Player
* ENHANCEMENT - Updated Citibanamex logo
* FIX - Blank screen in installments

## VERSION 4.9.3
_29_03_2019_
* FIX - Friction rate in id card

## VERSION 4.9.2
_28_03_2019_
* FIX - Automatic selection flow
* FIX - Correct amount for split payment on Congrats
* FIX - Crash on Circle Transform for certain images

## VERSION 4.9.1
_25_03_2019_
* FIX - Retry card storage

## VERSION 4.9.0
_21_03_2019_
* FEATURE - Issuer images in Cards
* FEATURE - CNPJ payment with Boleto
* FEATURE - Skip congrats in storage card flow

## VERSION 4.8.1
_18_03_2019_
* FIX - Crash on guessing when retrying payment

## VERSION 4.8.0
_12_03_2019_
* FEATURE - Account money discounts
* FEATURE - CPF validation in card guessing
* ENHANCEMENT - Soldout discount communication
* ENHANCEMENT - Changes in Loyalty flow
* ENHANCEMENT - Much lower assets weight
* FIX - Money In UI fixes

## VERSION 4.7.3
_11_03_2019_
* FIX - disable back button on exploding animation

## VERSION 4.7.2
_27_02_2019_
* FIX - payment processor bundle mapping fix for vending
* FIX - animations split payment
* FIX - invalid state cardvault

## VERSION 4.7.1
_27_02_2019_
* FIX - signature card storage
* FIX - identification only cpf for brazil
* FIX - event data review and confirm

## VERSION 4.7.0
_22_02_2019_
* FEATURE - Split Payment.
* FEATURE - CPF Validation.
* ENHANCEMENT - Added abort and action events for congrats / business.
* FIX - Terms and conditions event data.
* FIX - Animations in PaymentVaultActivity.
* FIX - Loading identification types NPE.
* FIX - Add new method drives to groups if cards isn't present.

## VERSION 4.6.2
_12_02_2019_
* FIX - Added credit card date validation.  
* ENHANCEMENT - Added tracks. 
* ENHANCEMENT - Added discount terms and conditions.
* FIX - Rollback public method.
* FIX - Connectivity manager.
* FIX - Activity new flag support - Android 9.

## VERSION 4.6.1
_04_02_2019_
* FIX - Added correct discount id in PaymentData.
* FIX - Attached view when exploding animation finished.

## VERSION 4.5.2
_31_01_2019_
* FIX - NPE tracking events.

## VERSION 4.6.0
_24_01_2019_
* FEATURE - Support to payment method discount.

## VERSION 4.5.1
_10_01_2019_
* FIX - Crash in groups disk cache.

## VERSION 4.5.0
_02_01_2019_
* FEATURE - Account money as a first class member.
* FEATURE - Added event and view data for PXTrackingListener class.
* FIX - Crash on back on Sec code saved card.

## VERSION 4.4.1
_18_12_2018_

* FIX - Navigation on payment method changed
* FIX - Crash on back from payment vault

## VERSION 4.4.0
_13_12_2018_

* FIX - Installments list clip in groups flow
* FIX - Invalid tracks
* ENHANCEMENT - Check for additional info for payer
* ENHANCEMENT - Better deploy scripts

## VERSION 4.3.3
_4_12_2018_

* FIX - Crash in Checkout, PaymentResult and BusinessResult on application kill

## VERSION 4.3.2
_30_11_2018_

* FIX - Crash on Card Association congrats

## VERSION 4.3.1
_21_11_2018_

* FIX - Installments selection in express flow
* FIX - Account money invested in express flow
* FIX - PEC and payer information assets
* FIX - Instructions padding

## VERSION 4.3.0
_31_10_2018_

 * FEATURE - Express checkout.
 * FEATURE - Skip Payer Information.
 * FEATURE - Pec Payment Method.
 * FEATURE - Enabled MLU (Site Uruguay)
 * FEATURE - Interactive instructions in congrats.
 * FEATURE - Configurable titles for Payment Vault Screen.
 * FEATURE - added new tracking listener PXTrackingListener for MeliData compatibility
 ```java
  void setListener(@NonNull final PXTrackingListener listener,
              @NonNull final Map<String, ? extends Object> flowDetail, 
              @Nullable final String flowName)
```

## VERSION 4.2.1
_30_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.2.0
_25_10_2018_

* FEATURE - dynamic custom dialogs for certain locations.
* FEATURE - dynamic custom views for review and confirm.
* ENHANCEMENT - tracking screen's names and paths unified.
* ENHANCEMENT/FIX - card addition flow now supports installments and ESC.

## VERSION 4.1.3
_01_11_2018_

* FIX - Crash NPE on exploding button

## VERSION 4.1.2
_31_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.1.0
_04_10_2018_

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* ENHANCEMENT - Standalone Card Association

## VERSION 4.0.6
_08_10_2018_

* FIX - Double congrats one tap.

## VERSION 4.0.5

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* FIX - Payment processor - visual attach bug

## VERSION 4.0.4
_25_09_2018_

* FIX - DefaultPaymentTypeId debit card error, can't look for settings.
* FIX - Colombia currency utils.

## VERSION 4.0.3

_20_09_2018_

* FIX - Payment recovery call for auth.
* FIX - destroy activity behaviour.
* FIX - NPE no decimals for Site Colombia.
* ENHANCEMENT - Color customization detailed documentation.
* ENHANCEMENT - Loading improvements for visual payments (payment processor).

## VERSION 4.0.2

_05_09_2018_

* Fix: one tap with payment recovery
* Fix: payment processor background support
* Fix: added internal payment method change behaviour


## VERSION 4.0.1

_03_09_2018_

* Fix: dynamic id declaration
* Fix: code discount
* Fix: esc with one tap

## VERSION 4.0.0

_30_08_2018_
