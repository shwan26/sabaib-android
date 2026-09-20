// Deletes the calling user's account entirely: cascades their bills/groups
// data, removes their promptpay-qrs storage objects, deletes their profile
// row, and finally deletes the Supabase Auth user itself.
//
// Deploy with:
//   supabase functions deploy delete-account
//
// Requires these secrets set on the project (never shipped in the app):
//   supabase secrets set SUPABASE_URL=<project-url> \
//     SUPABASE_ANON_KEY=<anon-key> SERVICE_ROLE_KEY=<service-role-key>

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const PROMPTPAY_QR_BUCKET = "promptpay-qrs";

Deno.serve(async (req) => {
  const authHeader = req.headers.get("Authorization") ?? "";

  // Verify the caller's own JWT to get their user id - never trust a
  // client-supplied id, since that would let anyone delete anyone's account.
  const callerClient = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_ANON_KEY")!,
    { global: { headers: { Authorization: authHeader } } },
  );

  const {
    data: { user },
    error: authError,
  } = await callerClient.auth.getUser();

  if (authError || !user) {
    return new Response(JSON.stringify({ error: "Unauthorized" }), {
      status: 401,
    });
  }

  const userId = user.id;

  const admin = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SERVICE_ROLE_KEY")!,
  );

  const { data: ownedBills, error: ownedBillsError } = await admin
    .from("bills")
    .select("id")
    .eq("owner_id", userId);

  if (ownedBillsError) {
    return new Response(JSON.stringify({ error: ownedBillsError.message }), {
      status: 500,
    });
  }

  const billIds = (ownedBills ?? []).map((bill) => bill.id as string);

  if (billIds.length > 0) {
    const { data: items } = await admin
      .from("receipt_items")
      .select("id")
      .in("bill_id", billIds);
    const itemIds = (items ?? []).map((item) => item.id as string);

    if (itemIds.length > 0) {
      await admin.from("item_claims").delete().in("item_id", itemIds);
    }

    await admin.from("participants").delete().in("bill_id", billIds);
    await admin.from("receipt_items").delete().in("bill_id", billIds);

    await admin.storage
      .from(PROMPTPAY_QR_BUCKET)
      .remove(billIds.map((billId) => `${billId}/qr`));

    await admin.from("bills").delete().in("id", billIds);
  }

  // Rows where this user joined someone else's bill as a participant.
  await admin.from("participants").delete().eq("user_id", userId);

  await admin.from("profiles").delete().eq("id", userId);

  const { error: deleteUserError } = await admin.auth.admin.deleteUser(
    userId,
  );

  if (deleteUserError) {
    return new Response(JSON.stringify({ error: deleteUserError.message }), {
      status: 500,
    });
  }

  return new Response(JSON.stringify({ ok: true }), { status: 200 });
});
