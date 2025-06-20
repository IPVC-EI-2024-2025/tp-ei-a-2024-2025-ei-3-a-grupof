import {serve} from "https://deno.land/std/http/server.ts"
import {createClient} from "https://esm.sh/@supabase/supabase-js"

serve(async (req) => {
    const {email, password, user_metadata = {}} = await req.json()

    const supabaseAdmin = createClient(
        Deno.env.get("SUPABASE_URL")!,
        Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    )

    const {data, error} = await supabaseAdmin.auth.admin.createUser({
        email,
        password,
        email_confirm: true,
        user_metadata
    })

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