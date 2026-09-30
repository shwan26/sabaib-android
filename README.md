# SabaiB
## Inspiration
In Thailand, eating out with friends almost always ends the same way: one person pays the whole bill, then spends the next 20 minutes typing receipt lines into a group chat, adding the service charge and VAT, and chasing everyone for money. It gets worse when you travel and can't even read the receipt.

*Sabai* (สบาย) means relaxed or at ease in Thai. I wanted splitting the **B**ill to feel sabai, so the motto became **"Split the bill, not the mood."**

## What it does
SabaiB turns a photo of a receipt into a fair, itemized split that everyone can pay in seconds.

- **Scan a receipt in any language.** Gemini reads the photo, pulls out every line item, and **translates each one into English** while keeping the original name, so travellers and mixed-language groups know exactly what they're paying for.
- **Review and fix.** Edit names, quantities, and prices, remove lines, and set the service charge, VAT, and discounts.
- **Open a bill room.** Friends join from their own phones with a group code or QR.
- **Claim what you ate.** Each person taps their items. Shared dishes split automatically between everyone who claimed them, or the host can split the bill evenly.
- **Payment Status** Each participant gets their exact total (and optional a PromptPay QR of the host), and the host can see who has paid.
- **Groups and history.** Recurring friend groups and past payments stay in one place.
- **SabaiB+.** Free users get 1 AI scan every 30 days. SabaiB+ unlocks unlimited scans as a monthly subscription through RevenueCat.

### The math behind a fair split
For an item \( i \) with unit price \( p_i \) and quantity \( q_i \), the service charge rate \( s \) and VAT rate \( v \) are folded into each item (with \( v = 0 \) when VAT is already included):

$$
\tilde{p}_i = p_i \, q_i \,(1 + s)(1 + v)
$$

If \( S_i \) is the set of people who claimed item \( i \), then person \( k \)'s share before discounts is

$$
F_k = \sum_{i \,:\, k \in S_i} \frac{\tilde{p}_i}{|S_i|}
$$

A bill-level discount \( D \) is shared in proportion to each person's raw food cost, so the person who ate more also gets more of the discount:

$$
T_k = F_k - D \cdot \frac{\displaystyle\sum_{i \,:\, k \in S_i} \frac{p_i q_i}{|S_i|}}{\displaystyle\sum_i p_i q_i}
$$

That way nobody overpays because they shared a dish or ordered something cheap.

## How we built it
- **Kotlin and Jetpack Compose** for a fully native Android UI, using MVVM with `StateFlow`
- **Supabase** (Postgres, Auth, Edge Functions) for accounts, profiles, groups, and the shared bill rooms that sync between the host's and participants' phones
- **Gemini vision API** with a strict JSON response schema for receipt parsing and translation, plus **ML Kit** text recognition and a regex parser as an on-device fallback
- **RevenueCat** with Google Play Billing for the SabaiB+ subscription, with RevenueCat's app user ID aliased to the Supabase user ID so a purchase follows the account

## Challenges we ran into
- **Real-time sync.** Keeping the host's and every participant's view consistent while several people claim items at once took several rounds of fixes to the join, claim, and payment flows.
- **Messy receipts.** Receipts mix scripts, abbreviations, local number formats (is `1.200` one point two or one thousand two hundred?), and metadata lines. Prompting Gemini with a schema, and letting users correct the result instead of trusting it blindly, made scanning reliable.
- **On-device OCR limits.** ML Kit can't read Thai script, so the fallback recovers prices, and the AI path handles names and translation.
- **Privacy and trust.** I added explicit consent before any image is sent to AI, an age gate, and full in-app account deletion through a Supabase Edge Function.
- **Monetization without breaking the social loop.** Everyone at the table has to be able to join for free, so only the host-side feature that actually costs money (AI scanning) sits behind the paywall.

## Accomplishments that I'm proud of
- Shipping a complete end-to-end flow on my own: **scan → translate → claim → pay → subscribe**
- A split that is actually fair, including shared dishes, service charge, VAT, and proportional discounts
- Multi-device bill rooms that feel live
- Translating receipts in any language, which makes SabaiB useful for travellers as well as locals
- A working RevenueCat subscription with restore purchases and account-linked entitlements

## What I learned
- How to design a backend schema and sync model for multi-user, real-time features with Supabase
- How to make an LLM reliable in production: structured JSON schemas, clear extraction rules, and always letting the user edit the output
- How to integrate RevenueCat and Google Play Billing, and how to think about pricing and paywall placement
- That privacy features like consent screens and account deletion should be designed in from the start, not added at the end

## What's next for SabaiB
- An **iOS version** so whole friend groups can join, whatever phone they use
- **Currency conversion** for travellers, showing totals in both the local and home currency
- **LINE sharing** and payment reminders for friends who haven't paid yet
- **Multiple receipts in one trip** with a running "who owes whom" balance
- **Galaxy Store** release and more local payment rails beyond PromptPay
