import {serve} from "https://deno.land/std/http/server.ts"
import {createClient} from "https://esm.sh/@supabase/supabase-js"

serve(async (req) => {
  const {user_id, email, password, user_metadata = {}} = await req.json()

  if (!user_id) {
    return new Response(JSON.stringify({error: "user_id is required"}), {
      status: 400,
      headers: {"Content-Type": "application/json"}
    })
  }

  const supabaseAdmin = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
  )

  const updateData = {
    user_metadata
  }

  if (email) updateData.email = email
  if (password) updateData.password = password

  const {data, error} = await supabaseAdmin.auth.admin.updateUserById(
      user_id,
      updateData
  )

  if (error) {
    return new Response(JSON.stringify({error: error.message}), {
      status: 400,
      headers: {"Content-Type": "application/json"}
    })
  }

  return new Response(JSON.stringify(data), {
    headers: {"Content-Type": "application/json"},
  })
})